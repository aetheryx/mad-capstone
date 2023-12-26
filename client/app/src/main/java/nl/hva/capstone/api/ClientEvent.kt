package nl.hva.capstone.api

import kotlinx.serialization.Serializable
import nl.hva.capstone.api.model.input.IncomingCallOffer

@Serializable
sealed class ClientEvent {
  @Serializable
  data class CallOffer(val data: IncomingCallOffer): ClientEvent()
  @Serializable
  data class CallResponse(val data: CallResponse): ClientEvent()
  @Serializable
  data class WebRTCPayload(val data: WebRTCPayload): ClientEvent()
}