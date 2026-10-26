package dev.mufraggi.agentique.core.memory

import arrow.core.Either
import dev.mufraggi.agentique.core.conversation.StorageError

/** A short fact about the user that the agent chose to retain. */
data class Memory(val id: MemoryId, val text: MemoryText)

/** Shown to the model as `[m12]` so that it can forget a precise memory. */
@JvmInline
value class MemoryId(val value: Long) {
    override fun toString() = "m$value"
}

/** All of the agent's memories. */
interface MemoryBank {
    suspend fun all(): Either<StorageError, List<Memory>>
    suspend fun add(text: MemoryText): Either<StorageError, Memory>
}
