package dev.mufraggi.agentique.core.provider

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow

/** Replies to successive requests with the scripted chunk lists, in order, and records the requests. */
class FakeProvider(vararg replies: List<ProviderChunk>) : Provider {
    private val replies = ArrayDeque(replies.toList())
    val requests = mutableListOf<ProviderRequest>()

    override fun stream(request: ProviderRequest): Flow<ProviderChunk> {
        requests += request
        val reply = replies.removeFirstOrNull()
            ?: throw AssertionError("FakeProvider received an unscripted request #${requests.size}")
        return reply.asFlow()
    }
}
