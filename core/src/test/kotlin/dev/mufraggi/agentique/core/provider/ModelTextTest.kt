package dev.mufraggi.agentique.core.provider

import arrow.core.nonEmptyListOf
import dev.mufraggi.agentique.core.conversation.StorageError
import dev.mufraggi.agentique.core.memory.MemoryTextError
import dev.mufraggi.agentique.core.tool.FieldError
import dev.mufraggi.agentique.core.tool.JsonType
import dev.mufraggi.agentique.core.tool.RememberError
import dev.mufraggi.agentique.core.tool.ToolArgumentError
import dev.mufraggi.agentique.core.tool.ToolError
import dev.mufraggi.agentique.core.tool.ToolFailure
import dev.mufraggi.agentique.core.tool.ToolOutcome
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

/** The only place where a tool outcome becomes the text the model reads. */
class ModelTextTest {
    @Test
    fun `a success is its content, as is`() {
        ToolOutcome.Success("Remembered as [m1].").toModelText() shouldBe "Remembered as [m1]."
    }

    @Test
    fun `an unknown tool lists the available ones`() {
        ToolOutcome.Failure(ToolFailure.UnknownTool("teleport", listOf("echo", "remember")))
            .toModelText() shouldBe
            "Error: there is no tool named \"teleport\". Available tools: echo, remember."
    }

    @Test
    fun `invalid arguments list every problem`() {
        ToolOutcome.Failure(
            ToolFailure.InvalidArguments(
                "remember",
                nonEmptyListOf(
                    ToolArgumentError.Missing("fact"),
                    ToolArgumentError.WrongType("when", JsonType.String),
                    ToolArgumentError.Invalid("fact", FieldError.Memory(MemoryTextError.Blank)),
                    ToolArgumentError.Invalid("fact", FieldError.Memory(MemoryTextError.MultiLine)),
                    ToolArgumentError.Invalid("fact", FieldError.Memory(MemoryTextError.TooLong(201))),
                ),
            ),
        ).toModelText() shouldBe
            "Error: invalid arguments for \"remember\": " +
            "missing field \"fact\"; " +
            "field \"when\" must be a string; " +
            "field \"fact\" must not be blank; " +
            "field \"fact\" must be a single line; " +
            "field \"fact\" must be at most 200 characters, got 201."
    }

    @Test
    fun `a tool failure explains what the model can do`() {
        ToolOutcome.Failure(ToolFailure.ExecutionFailed("remember", RememberError.MemoryBankFull))
            .toModelText() shouldBe
            "Error: \"remember\" failed: the memory bank is full (100 memories). " +
            "Forget an outdated memory first, or tell the user."
        ToolOutcome.Failure(ToolFailure.ExecutionFailed("remember", ToolError.Storage(StorageError.Corrupted("row 3"))))
            .toModelText() shouldBe
            "Error: \"remember\" failed: the app's storage is unavailable. Tell the user."
    }
}
