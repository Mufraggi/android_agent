package dev.mufraggi.agentique.llm

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class ModelsResponseTest {
    @Test
    fun `decodes an OpenRouter models list and ignores unknown fields`() {
        val body = """
            {"data": [
              {"id": "anthropic/claude-sonnet-5.5", "context_length": 200000,
               "supported_parameters": ["tools", "temperature"]},
              {"id": "llama3.2", "object": "model"}
            ]}
        """.trimIndent()

        ProviderJson.decodeFromString(ModelsResponse.serializer(), body) shouldBe ModelsResponse(
            listOf(
                ModelDto("anthropic/claude-sonnet-5.5", listOf("tools", "temperature")),
                ModelDto("llama3.2", null),
            ),
        )
    }
}
