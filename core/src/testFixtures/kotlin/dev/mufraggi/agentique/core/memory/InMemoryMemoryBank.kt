package dev.mufraggi.agentique.core.memory

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import dev.mufraggi.agentique.core.conversation.StorageError

/** Hands out ids `m1`, `m2`… like the database's auto-increment. */
class InMemoryMemoryBank : MemoryBank {
    private val memories = mutableListOf<Memory>()
    private var nextId = 1L
    private var failure: StorageError? = null

    /** Every later call returns [error]. */
    fun failWith(error: StorageError) {
        failure = error
    }

    override suspend fun all(): Either<StorageError, List<Memory>> =
        failure?.left() ?: memories.toList().right()

    override suspend fun add(text: MemoryText): Either<StorageError, Memory> =
        failure?.left() ?: Memory(MemoryId(nextId++), text).also { memories += it }.right()
}
