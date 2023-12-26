package nl.hva.capstone.api

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import nl.hva.capstone.api.model.input.IncomingCallOffer

@Serializable
sealed class ClientEvent {
  @Serializable
  data class CallOffer(val data: IncomingCallOffer): ClientEvent()
  @Serializable
  data class CallResponse(val data: CallResponse): ClientEvent()
  @Serializable
  data class WebRTCPayload(val data: WebRTCPayload): ClientEvent()

  fun toJSON() = buildJsonObject {
    val self = this@ClientEvent
    put("event", self::class.simpleName!!)

    val data = Json.encodeToJsonElement(self).jsonObject["data"]!!
    put("data", data)
  }
}