package dev.mufraggi.agentique.core.provider

import dev.mufraggi.agentique.core.memory.MemoryText
import dev.mufraggi.agentique.core.memory.MemoryTextError
import dev.mufraggi.agentique.core.memory.RememberTool
import dev.mufraggi.agentique.core.tool.FieldError
import dev.mufraggi.agentique.core.tool.JsonType
import dev.mufraggi.agentique.core.tool.RememberError
import dev.mufraggi.agentique.core.tool.ToolArgumentError
import dev.mufraggi.agentique.core.tool.ToolError
import dev.mufraggi.agentique.core.tool.ToolFailure
import dev.mufraggi.agentique.core.tool.ToolOutcome

/**
 * The text the model reads for a tool outcome. Provider adapters call this when building
 * a request; nothing else in the domain turns an error into text.
 */
fun ToolOutcome.toModelText(): String = when (this) {
    is ToolOutcome.Success -> content
    is ToolOutcome.Failure -> "Error: ${failure.text()}"
}

private fun ToolFailure.text(): String = when (this) {
    is ToolFailure.UnknownTool ->
        "there is no tool named \"$name\". Available tools: ${available.joinToString()}."
    is ToolFailure.InvalidArguments ->
        "invalid arguments for \"$tool\": ${errors.joinToString("; ") { it.text() }}."
    is ToolFailure.ExecutionFailed -> "\"$tool\" failed: ${error.text()}"
}

private fun ToolArgumentError.text(): String = when (this) {
    is ToolArgumentError.Missing -> "missing field \"$field\""
    is ToolArgumentError.WrongType -> "field \"$field\" must be ${expected.text()}"
    is ToolArgumentError.Invalid -> "field \"$field\" ${error.text()}"
}

private fun JsonType.text(): String = when (this) {
    JsonType.String -> "a string"
    JsonType.Number -> "a number"
    JsonType.Integer -> "an integer"
    JsonType.Boolean -> "a boolean"
    JsonType.Object -> "an object"
    JsonType.Array -> "an array"
}

private fun FieldError.text(): String = when (this) {
    is FieldError.Memory -> when (error) {
        MemoryTextError.Blank -> "must not be blank"
        MemoryTextError.MultiLine -> "must be a single line"
        is MemoryTextError.TooLong -> "must be at most ${MemoryText.MAX_LENGTH} characters, got ${error.length}"
    }
}

private fun ToolError.text(): String = when (this) {
    is ToolError.Storage -> "the app's storage is unavailable. Tell the user."
    RememberError.MemoryBankFull ->
        "the memory bank is full (${RememberTool.CAPACITY} memories). " +
            "Forget an outdated memory first, or tell the user."
}
