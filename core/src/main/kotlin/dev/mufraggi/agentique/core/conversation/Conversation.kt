package dev.mufraggi.agentique.core.conversation

import arrow.core.Either
import dev.mufraggi.agentique.core.tool.ToolCall
import dev.mufraggi.agentique.core.tool.ToolCallId
import dev.mufraggi.agentique.core.tool.ToolOutcome

@JvmInline
value class ConversationId(val value: String) {
    companion object {
        /** The single free conversation between the user and the agent. */
        val Main = ConversationId("main")
    }
}

sealed interface Message {
    data class User(val text: String) : Message
    data class Agent(val text: String, val toolCalls: List<ToolCall> = emptyList()) : Message
    data class ToolResult(val callId: ToolCallId, val outcome: ToolOutcome) : Message
}

interface ConversationRepository {
    suspend fun messages(id: ConversationId): Either<StorageError, List<Message>>
    suspend fun append(id: ConversationId, message: Message): Either<StorageError, Unit>
}

sealed interface StorageError {
    /** A stored row no longer parses into a domain type. */
    data class Corrupted(val detail: String) : StorageError
}
