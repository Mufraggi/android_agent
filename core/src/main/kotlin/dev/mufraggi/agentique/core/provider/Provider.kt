package dev.mufraggi.agentique.core.provider

import dev.mufraggi.agentique.core.conversation.Message
import dev.mufraggi.agentique.core.memory.Memory
import dev.mufraggi.agentique.core.tool.ToolCall
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.JsonObject

/** The service or runtime that executes a model; replies are streamed. */
interface Provider {
    fun stream(request: ProviderRequest): Flow<ProviderChunk>
}

/** [memories] are all of them, injected into the system prompt by the adapter (ADR 0009). */
data class ProviderRequest(
    val messages: List<Message>,
    val tools: List<ToolSpec> = emptyList(),
    val memories: List<Memory> = emptyList(),
)

/** What the model is told about a tool it may call. */
data class ToolSpec(val name: String, val description: String, val parameters: JsonObject)

sealed interface ProviderChunk {
    data class TextDelta(val text: String) : ProviderChunk

    /** A complete tool call; the adapter assembles streamed argument fragments itself. */
    data class ToolCallRequested(val call: ToolCall) : ProviderChunk
}
