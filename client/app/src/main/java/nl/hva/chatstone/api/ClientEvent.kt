package nl.hva.chatstone.api

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put
import nl.hva.chatstone.api.model.CallResponse
import nl.hva.chatstone.api.model.WebRTCPayload
import nl.hva.chatstone.api.model.input.IncomingCallOffer

@Serializable
sealed class ClientEvent {
  @Serializable
  data class CallOfferEvent(val data: IncomingCallOffer): ClientEvent()

  @Serializable
  data class CallResponseEvent(val data: CallResponse): ClientEvent()

  @Serializable
  class CallHangupEvent(): ClientEvent()

  @Serializable
  data class WebRTCPayloadEvent(val data: WebRTCPayload): ClientEvent()

  fun toJSON() = buildJsonObject {
    val self = this@ClientEvent
    put("event", self::class.simpleName!!.replace("Event", ""))

    val data = Json.encodeToJsonElement(self).jsonObject["data"]!!
    put("data", data)
  }
}