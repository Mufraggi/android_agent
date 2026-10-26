package dev.mufraggi.agentique.core.memory

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure

/** The text of a [Memory]: one trimmed, non-blank line of at most [MAX_LENGTH] characters. */
@JvmInline
value class MemoryText private constructor(val value: String) {
    companion object {
        const val MAX_LENGTH = 200

        fun parse(raw: String): Either<MemoryTextError, MemoryText> = either {
            val text = raw.trim()
            ensure(text.isNotEmpty()) { MemoryTextError.Blank }
            ensure(text.none { it == '\n' || it == '\r' }) { MemoryTextError.MultiLine }
            ensure(text.length <= MAX_LENGTH) { MemoryTextError.TooLong(text.length) }
            MemoryText(text)
        }
    }
}

sealed interface MemoryTextError {
    data object Blank : MemoryTextError
    data object MultiLine : MemoryTextError
    data class TooLong(val length: Int) : MemoryTextError
}
