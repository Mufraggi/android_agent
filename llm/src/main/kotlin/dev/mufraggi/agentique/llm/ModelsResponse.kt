package dev.mufraggi.agentique.llm

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** Providers add fields freely; only the ones read here are part of the contract. */
internal val ProviderJson = Json { ignoreUnknownKeys = true }

/** Body of `GET /models` on an OpenAI-compatible API. */
@Serializable
internal data class ModelsResponse(val data: List<ModelDto>)

@Serializable
internal data class ModelDto(
    val id: String,
    /** Only some providers (OpenRouter) list it; `null` means unknown, not "no tools". */
    @SerialName("supported_parameters") val supportedParameters: List<String>? = null,
)
