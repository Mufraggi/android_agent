package dev.mufraggi.agentique.core.memory

import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.left
import arrow.core.nonEmptyListOf
import arrow.core.raise.either
import arrow.core.raise.ensure
import dev.mufraggi.agentique.core.tool.FieldError
import dev.mufraggi.agentique.core.tool.JsonType
import dev.mufraggi.agentique.core.tool.RememberError
import dev.mufraggi.agentique.core.tool.Tool
import dev.mufraggi.agentique.core.tool.ToolArgumentError
import dev.mufraggi.agentique.core.tool.ToolError
import dev.mufraggi.agentique.core.tool.ToolResult
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject

class RememberTool(private val memories: MemoryBank) : Tool<MemoryText> {
    override val name = "remember"
    override val description =
        "Remember a short fact about the user for all future conversations. One fact per call."
    override val parameters = buildJsonObject {
        put("type", "object")
        putJsonObject("properties") {
            putJsonObject("fact") {
                put("type", "string")
                put("description", "One line, at most ${MemoryText.MAX_LENGTH} characters.")
            }
        }
        putJsonArray("required") { add(JsonPrimitive("fact")) }
    }

    override fun parse(arguments: JsonObject): Either<NonEmptyList<ToolArgumentError>, MemoryText> {
        val fact = arguments["fact"] ?: return nonEmptyListOf(ToolArgumentError.Missing("fact")).left()
        if (fact !is JsonPrimitive || !fact.isString) {
            return nonEmptyListOf(ToolArgumentError.WrongType("fact", JsonType.String)).left()
        }
        return MemoryText.parse(fact.content)
            .mapLeft { nonEmptyListOf(ToolArgumentError.Invalid("fact", FieldError.Memory(it))) }
    }

    override suspend fun execute(arguments: MemoryText): Either<ToolError, ToolResult> = either {
        val count = memories.all().mapLeft(ToolError::Storage).bind().size
        ensure(count < CAPACITY) { RememberError.MemoryBankFull }
        val memory = memories.add(arguments).mapLeft(ToolError::Storage).bind()
        ToolResult("Remembered as [${memory.id}].")
    }

    companion object {
        /** Every memory is injected into every request, so the bank stays small (ADR 0009). */
        const val CAPACITY = 100
    }
}
