package dev.mufraggi.agentique.core.run

import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.left
import arrow.core.nonEmptyListOf
import arrow.core.right
import dev.mufraggi.agentique.core.conversation.ConversationId
import dev.mufraggi.agentique.core.conversation.InMemoryConversationRepository
import dev.mufraggi.agentique.core.conversation.Message
import dev.mufraggi.agentique.core.conversation.StorageError
import dev.mufraggi.agentique.core.memory.InMemoryMemoryBank
import dev.mufraggi.agentique.core.memory.MemoryText
import dev.mufraggi.agentique.core.memory.RememberTool
import dev.mufraggi.agentique.core.provider.FakeProvider
import dev.mufraggi.agentique.core.provider.ProviderChunk
import dev.mufraggi.agentique.core.provider.ToolSpec
import dev.mufraggi.agentique.core.tool.Tool
import dev.mufraggi.agentique.core.tool.ToolArgumentError
import dev.mufraggi.agentique.core.tool.ToolCall
import dev.mufraggi.agentique.core.tool.ToolCallId
import dev.mufraggi.agentique.core.tool.ToolError
import dev.mufraggi.agentique.core.tool.ToolFailure
import dev.mufraggi.agentique.core.tool.ToolOutcome
import dev.mufraggi.agentique.core.tool.ToolResult
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import org.junit.jupiter.api.Test

class RunAgentTest {
    private val conversations = InMemoryConversationRepository()
    private val memories = InMemoryMemoryBank()

    @Test
    fun `a text reply completes the run and is saved in the conversation`() = runTest {
        val provider = FakeProvider(
            listOf(ProviderChunk.TextDelta("Bon"), ProviderChunk.TextDelta("jour !")),
        )
        val runAgent = RunAgent(provider, conversations, memories)

        val finished = runAgent(RunTrigger.UserMessage(ConversationId.Main, "Salut")).last()

        finished shouldBe RunEvent.Finished(RunOutcome.Completed)
        conversations.messages(ConversationId.Main).shouldBeRight() shouldBe listOf(
            Message.User("Salut"),
            Message.Agent("Bonjour !"),
        )
    }

    @Test
    fun `existing memories are sent to the provider with the request`() = runTest {
        val vegetarian = memories.add(MemoryText.parse("Est végétarien").shouldBeRight()).shouldBeRight()
        val lyon = memories.add(MemoryText.parse("Habite à Lyon").shouldBeRight()).shouldBeRight()
        val provider = FakeProvider(listOf(ProviderChunk.TextDelta("Une ratatouille ?")))

        RunAgent(provider, conversations, memories)(RunTrigger.UserMessage(ConversationId.Main, "On mange quoi ?")).last()

        provider.requests.single().memories shouldBe listOf(vegetarian, lyon)
    }

    @Test
    fun `a memory remembered during the run is sent with the next model call`() = runTest {
        val call = ToolCall(ToolCallId("call_1"), "remember", buildJsonObject { put("fact", "Habite à Lyon") })
        val provider = FakeProvider(
            listOf(ProviderChunk.ToolCallRequested(call)),
            listOf(ProviderChunk.TextDelta("C'est noté.")),
        )

        RunAgent(provider, conversations, memories, tools = listOf(RememberTool(memories)))(
            RunTrigger.UserMessage(ConversationId.Main, "J'habite à Lyon"),
        ).last()

        provider.requests.map { request -> request.memories.map { it.text.value } } shouldBe listOf(
            emptyList(),
            listOf("Habite à Lyon"),
        )
    }

    @Test
    fun `a storage error fails the run without calling the provider`() = runTest {
        val provider = FakeProvider()
        val corrupted = StorageError.Corrupted("message 3 has no text")
        conversations.failWith(corrupted)

        val finished = RunAgent(provider, conversations, memories)(RunTrigger.UserMessage(ConversationId.Main, "Salut")).last()

        finished shouldBe RunEvent.Finished(RunOutcome.Failed(RunFailure.Storage(corrupted)))
        provider.requests shouldBe emptyList()
    }

    @Test
    fun `a requested tool runs and its result goes back to the model before the final answer`() = runTest {
        val call = ToolCall(ToolCallId("call_1"), "echo", buildJsonObject { put("text", "ping") })
        val provider = FakeProvider(
            listOf(ProviderChunk.ToolCallRequested(call)),
            listOf(ProviderChunk.TextDelta("Il a répondu pong.")),
        )

        val finished = RunAgent(provider, conversations, memories, tools = listOf(EchoTool, BrokenTool))(
            RunTrigger.UserMessage(ConversationId.Main, "Fais un echo"),
        ).last()

        finished shouldBe RunEvent.Finished(RunOutcome.Completed)
        conversations.messages(ConversationId.Main).shouldBeRight() shouldBe listOf(
            Message.User("Fais un echo"),
            Message.Agent("", listOf(call)),
            Message.ToolResult(ToolCallId("call_1"), ToolOutcome.Success("pong: ping")),
            Message.Agent("Il a répondu pong."),
        )
    }

    @Test
    fun `the model is told which tools it can call`() = runTest {
        val provider = FakeProvider(listOf(ProviderChunk.TextDelta("D'accord.")))

        RunAgent(provider, conversations, memories, tools = listOf(EchoTool))(
            RunTrigger.UserMessage(ConversationId.Main, "Salut"),
        ).last()

        provider.requests.single().tools shouldBe listOf(
            ToolSpec("echo", "Repeats the given text.", EchoTool.parameters),
        )
    }

    @Test
    fun `calling an unknown tool is reported to the model and the run goes on`() = runTest {
        val call = ToolCall(ToolCallId("call_1"), "teleport", buildJsonObject {})
        val provider = FakeProvider(
            listOf(ProviderChunk.ToolCallRequested(call)),
            listOf(ProviderChunk.TextDelta("Je ne peux pas faire ça.")),
        )

        val finished = RunAgent(provider, conversations, memories, tools = listOf(EchoTool, BrokenTool))(
            RunTrigger.UserMessage(ConversationId.Main, "Téléporte-moi"),
        ).last()

        finished shouldBe RunEvent.Finished(RunOutcome.Completed)
        provider.requests.last().messages.last() shouldBe Message.ToolResult(
            ToolCallId("call_1"),
            ToolOutcome.Failure(ToolFailure.UnknownTool("teleport", available = listOf("echo", "broken"))),
        )
    }

    @Test
    fun `invalid tool arguments are reported to the model, which can retry`() = runTest {
        val badCall = ToolCall(ToolCallId("call_1"), "echo", buildJsonObject { put("txt", "ping") })
        val goodCall = ToolCall(ToolCallId("call_2"), "echo", buildJsonObject { put("text", "ping") })
        val provider = FakeProvider(
            listOf(ProviderChunk.ToolCallRequested(badCall)),
            listOf(ProviderChunk.ToolCallRequested(goodCall)),
            listOf(ProviderChunk.TextDelta("pong")),
        )

        RunAgent(provider, conversations, memories, tools = listOf(EchoTool))(
            RunTrigger.UserMessage(ConversationId.Main, "Fais un echo"),
        ).last()

        conversations.messages(ConversationId.Main).shouldBeRight()
            .filterIsInstance<Message.ToolResult>() shouldBe listOf(
            Message.ToolResult(
                ToolCallId("call_1"),
                ToolOutcome.Failure(
                    ToolFailure.InvalidArguments("echo", nonEmptyListOf(ToolArgumentError.Missing("text"))),
                ),
            ),
            Message.ToolResult(ToolCallId("call_2"), ToolOutcome.Success("pong: ping")),
        )
    }

    @Test
    fun `a tool that fails while running is reported to the model and the run goes on`() = runTest {
        val call = ToolCall(ToolCallId("call_1"), "broken", buildJsonObject {})
        val provider = FakeProvider(
            listOf(ProviderChunk.ToolCallRequested(call)),
            listOf(ProviderChunk.TextDelta("Désolé, ça n'a pas marché.")),
        )

        val finished = RunAgent(provider, conversations, memories, tools = listOf(BrokenTool))(
            RunTrigger.UserMessage(ConversationId.Main, "Casse tout"),
        ).last()

        finished shouldBe RunEvent.Finished(RunOutcome.Completed)
        provider.requests.last().messages.last() shouldBe Message.ToolResult(
            ToolCallId("call_1"),
            ToolOutcome.Failure(ToolFailure.ExecutionFailed("broken", BrokenTool.error)),
        )
    }
}

private object EchoTool : Tool<String> {
    override val name = "echo"
    override val description = "Repeats the given text."
    override val parameters = buildJsonObject {
        put("type", "object")
        putJsonObject("properties") { putJsonObject("text") { put("type", "string") } }
    }

    override fun parse(arguments: JsonObject): Either<NonEmptyList<ToolArgumentError>, String> =
        arguments["text"]?.jsonPrimitive?.content?.right()
            ?: nonEmptyListOf(ToolArgumentError.Missing("text")).left()

    override suspend fun execute(arguments: String): Either<ToolError, ToolResult> =
        ToolResult("pong: $arguments").right()
}

/** Always fails while running, as a tool whose storage broke would. */
private object BrokenTool : Tool<Unit> {
    val error = ToolError.Storage(StorageError.Corrupted("row 1"))

    override val name = "broken"
    override val description = "Always fails."
    override val parameters = buildJsonObject { put("type", "object") }

    override fun parse(arguments: JsonObject): Either<NonEmptyList<ToolArgumentError>, Unit> = Unit.right()

    override suspend fun execute(arguments: Unit): Either<ToolError, ToolResult> = error.left()
}
