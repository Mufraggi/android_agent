package dev.mufraggi.agentique.core.conversation

import arrow.core.Either
import arrow.core.left
import arrow.core.right

class InMemoryConversationRepository : ConversationRepository {
    private val conversations = mutableMapOf<ConversationId, MutableList<Message>>()
    private var failure: StorageError? = null

    /** Every later call returns [error]. */
    fun failWith(error: StorageError) {
        failure = error
    }

    override suspend fun messages(id: ConversationId): Either<StorageError, List<Message>> =
        failure?.left() ?: conversations[id].orEmpty().toList().right()

    override suspend fun append(id: ConversationId, message: Message): Either<StorageError, Unit> =
        failure?.left() ?: conversations.getOrPut(id) { mutableListOf() }.add(message).let { Unit.right() }
}
