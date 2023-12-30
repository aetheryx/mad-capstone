package nl.hva.chatstone.api.model

import kotlinx.serialization.Serializable

@Serializable
data class WebRTCPayload(
  val payload: String
)