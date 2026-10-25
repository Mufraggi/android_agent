package dev.mufraggi.agentique.core.memory

import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class MemoryTextTest {
    @Test
    fun `trims surrounding whitespace`() {
        MemoryText.parse("  Habite à Lyon \n").shouldBeRight().value shouldBe "Habite à Lyon"
    }

    @Test
    fun `rejects blank text`() {
        MemoryText.parse("   ").shouldBeLeft(MemoryTextError.Blank)
    }

    @Test
    fun `rejects text spanning several lines`() {
        MemoryText.parse("Habite à Lyon\nTravaille à Paris").shouldBeLeft(MemoryTextError.MultiLine)
    }

    @Test
    fun `accepts text at the length limit and rejects one character more`() {
        MemoryText.parse("a".repeat(MemoryText.MAX_LENGTH)).shouldBeRight()
        MemoryText.parse("a".repeat(MemoryText.MAX_LENGTH + 1))
            .shouldBeLeft(MemoryTextError.TooLong(MemoryText.MAX_LENGTH + 1))
    }
}
