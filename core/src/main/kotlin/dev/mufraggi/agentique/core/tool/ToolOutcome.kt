package dev.mufraggi.agentique.core.tool

import arrow.core.NonEmptyList
import dev.mufraggi.agentique.core.conversation.StorageError
import dev.mufraggi.agentique.core.memory.MemoryTextError

/** How one tool call ended, as kept in the conversation and sent back to the model. */
sealed interface ToolOutcome {
    data class Success(val content: String) : ToolOutcome
    data class Failure(val failure: ToolFailure) : ToolOutcome
}

sealed interface ToolFailure {
    data class UnknownTool(val name: String, val available: List<String>) : ToolFailure
    data class InvalidArguments(val tool: String, val errors: NonEmptyList<ToolArgumentError>) : ToolFailure
    data class ExecutionFailed(val tool: String, val error: ToolError) : ToolFailure
}

sealed interface ToolArgumentError {
    data class Missing(val field: String) : ToolArgumentError
    data class WrongType(val field: String, val expected: JsonType) : ToolArgumentError
    data class Invalid(val field: String, val error: FieldError) : ToolArgumentError
}

enum class JsonType { String, Number, Integer, Boolean, Object, Array }

/**
 * A domain parsing error on one argument, kept as is. Sealed here rather than a marker
 * interface so that rendering and storage stay exhaustive.
 */
sealed interface FieldError {
    data class Memory(val error: MemoryTextError) : FieldError
}

/** Errors a tool can hit while running; one sub-hierarchy per tool, all sealed here for the same reason. */
sealed interface ToolError {
    data class Storage(val error: StorageError) : ToolError
}

sealed interface RememberError : ToolError {
    data object MemoryBankFull : RememberError
}
