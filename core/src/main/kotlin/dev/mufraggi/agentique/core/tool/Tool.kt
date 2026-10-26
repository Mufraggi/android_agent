package dev.mufraggi.agentique.core.tool

import arrow.core.Either
import arrow.core.NonEmptyList
import kotlinx.serialization.json.JsonObject

/**
 * A capability the agent can invoke. The model's raw [JsonObject] arguments are parsed once,
 * by the tool itself, into [A] before anything runs (ADR 0004).
 */
interface Tool<A> {
    val name: String
    val description: String

    /** JSON Schema of the arguments, sent to the model. */
    val parameters: JsonObject

    /** Reports every problem at once, so the model can fix them all in one retry. */
    fun parse(arguments: JsonObject): Either<NonEmptyList<ToolArgumentError>, A>

    suspend fun execute(arguments: A): Either<ToolError, ToolResult>
}

@JvmInline
value class ToolCallId(val value: String)

/** A tool invocation requested by the model, with its arguments still unparsed. */
data class ToolCall(val id: ToolCallId, val name: String, val arguments: JsonObject)

/** What a tool produced when it succeeded; free text by nature (an event list, a page…). */
data class ToolResult(val content: String)
