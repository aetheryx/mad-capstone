package nl.hva.capstone.api.model

import kotlinx.serialization.Serializable

@Serializable
data class WebRTCPayload(
  val payload: String
)