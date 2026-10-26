package dev.mufraggi.agentique.core.memory

import arrow.core.nonEmptyListOf
import dev.mufraggi.agentique.core.conversation.StorageError
import dev.mufraggi.agentique.core.tool.FieldError
import dev.mufraggi.agentique.core.tool.JsonType
import dev.mufraggi.agentique.core.tool.RememberError
import dev.mufraggi.agentique.core.tool.ToolArgumentError
import dev.mufraggi.agentique.core.tool.ToolError
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.junit.jupiter.api.Test

class RememberToolTest {
    private val memories = InMemoryMemoryBank()
    private val remember = RememberTool(memories)

    private fun fact(text: String) = MemoryText.parse(text).shouldBeRight()

    @Test
    fun `remembering a fact adds it to the memory bank and tells the model its id`() = runTest {
        val parsed = remember.parse(buildJsonObject { put("fact", " Habite à Lyon ") }).shouldBeRight()

        remember.execute(parsed).shouldBeRight().content shouldBe "Remembered as [m1]."
        memories.all().shouldBeRight().map { it.text.value } shouldBe listOf("Habite à Lyon")
    }

    @Test
    fun `a missing fact or one that is not a string is rejected`() {
        remember.parse(buildJsonObject {})
            .shouldBeLeft(nonEmptyListOf(ToolArgumentError.Missing("fact")))
        remember.parse(buildJsonObject { put("fact", 42) })
            .shouldBeLeft(nonEmptyListOf(ToolArgumentError.WrongType("fact", JsonType.String)))
    }

    @Test
    fun `a fact that is not a valid memory is rejected with the domain error`() {
        remember.parse(buildJsonObject { put("fact", "Habite à Lyon\nTravaille à Paris") })
            .shouldBeLeft(
                nonEmptyListOf(ToolArgumentError.Invalid("fact", FieldError.Memory(MemoryTextError.MultiLine))),
            )
    }

    @Test
    fun `nothing more is remembered once the memory bank holds 100 memories`() = runTest {
        repeat(100) { memories.add(fact("Fait n°$it")) }

        remember.execute(fact("Habite à Lyon")).shouldBeLeft(RememberError.MemoryBankFull)
        memories.all().shouldBeRight().size shouldBe 100
    }

    @Test
    fun `a storage failure is reported as a tool error`() = runTest {
        val corrupted = StorageError.Corrupted("memory 3 has no text")
        memories.failWith(corrupted)

        remember.execute(fact("Habite à Lyon")).shouldBeLeft(ToolError.Storage(corrupted))
    }
}
