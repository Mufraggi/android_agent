package dev.mufraggi.agentique.core.run

import arrow.core.getOrElse
import arrow.core.raise.either
import dev.mufraggi.agentique.core.conversation.ConversationId
import dev.mufraggi.agentique.core.conversation.ConversationRepository
import dev.mufraggi.agentique.core.conversation.Message
import dev.mufraggi.agentique.core.conversation.StorageError
import dev.mufraggi.agentique.core.memory.MemoryBank
import dev.mufraggi.agentique.core.provider.Provider
import dev.mufraggi.agentique.core.provider.ProviderChunk
import dev.mufraggi.agentique.core.provider.ProviderRequest
import dev.mufraggi.agentique.core.provider.ToolSpec
import dev.mufraggi.agentique.core.tool.Tool
import dev.mufraggi.agentique.core.tool.ToolCall
import dev.mufraggi.agentique.core.tool.ToolFailure
import dev.mufraggi.agentique.core.tool.ToolOutcome
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.JsonObject

sealed interface RunTrigger {
    val conversationId: ConversationId

    data class UserMessage(override val conversationId: ConversationId, val text: String) : RunTrigger
}

sealed interface RunEvent {
    data class Finished(val outcome: RunOutcome) : RunEvent
}

sealed interface RunOutcome {
    data object Completed : RunOutcome
    data class Failed(val cause: RunFailure) : RunOutcome
}

sealed interface RunFailure {
    data class Storage(val error: StorageError) : RunFailure
}

/** One full execution of the agent loop, from its trigger to the final answer. */
class RunAgent(
    private val provider: Provider,
    private val conversations: ConversationRepository,
    private val memories: MemoryBank,
    private val tools: List<Tool<*>> = emptyList(),
) {
    private val toolSpecs = tools.map { ToolSpec(it.name, it.description, it.parameters) }

    operator fun invoke(trigger: RunTrigger): Flow<RunEvent> = flow {
        val outcome = either {
            val id = trigger.conversationId
            when (trigger) {
                is RunTrigger.UserMessage -> conversations.append(id, Message.User(trigger.text)).bind()
            }
            do {
                val history = conversations.messages(id).bind()
                val remembered = memories.all().bind()
                val text = StringBuilder()
                val calls = mutableListOf<ToolCall>()
                provider.stream(ProviderRequest(history, toolSpecs, remembered)).collect { chunk ->
                    when (chunk) {
                        is ProviderChunk.TextDelta -> text.append(chunk.text)
                        is ProviderChunk.ToolCallRequested -> calls += chunk.call
                    }
                }
                conversations.append(id, Message.Agent(text.toString(), calls)).bind()
                for (call in calls) {
                    conversations.append(id, Message.ToolResult(call.id, execute(call))).bind()
                }
            } while (calls.isNotEmpty())
            RunOutcome.Completed
        }.getOrElse { RunOutcome.Failed(RunFailure.Storage(it)) }
        emit(RunEvent.Finished(outcome))
    }

    /** A failing tool call is an outcome for the model to react to, never a failed run. */
    private suspend fun execute(call: ToolCall): ToolOutcome {
        val tool = tools.find { it.name == call.name }
            ?: return ToolOutcome.Failure(ToolFailure.UnknownTool(call.name, tools.map { it.name }))
        return tool.call(call.arguments)
    }

    private suspend fun <A> Tool<A>.call(arguments: JsonObject): ToolOutcome =
        parse(arguments).fold({ ToolOutcome.Failure(ToolFailure.InvalidArguments(name, it)) }) { parsed ->
            execute(parsed).fold(
                { ToolOutcome.Failure(ToolFailure.ExecutionFailed(name, it)) },
                { ToolOutcome.Success(it.content) },
            )
        }
}
