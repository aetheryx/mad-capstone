package nl.hva.capstone.api

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import nl.hva.capstone.api.model.CallResponse
import nl.hva.capstone.api.model.WebRTCPayload
import nl.hva.capstone.api.model.output.ConversationMessage
import nl.hva.capstone.api.model.output.OutgoingCallOffer

@Serializable
sealed class ServerEvent {
  @Serializable
  data class MessageCreateEvent(val data: ConversationMessage) : ServerEvent()
  @Serializable
  data class CallOfferEvent(val data: OutgoingCallOffer): ServerEvent()
  @Serializable
  data class CallResponseEvent(val data: CallResponse): ServerEvent()
  @Serializable
  data class WebRTCPayloadEvent(val data: WebRTCPayload): ServerEvent()

  companion object : JsonContentPolymorphicSerializer<ServerEvent>(ServerEvent::class) {
    override fun selectDeserializer(element: JsonElement) =
      when (element.jsonObject["event"]?.jsonPrimitive?.content) {
        "MessageCreate" -> MessageCreateEvent.serializer()
        "CallOffer" -> CallOfferEvent.serializer()
        "CallResponse" -> CallResponseEvent.serializer()
        "WebRTCPayload" -> WebRTCPayloadEvent.serializer()

        else -> error("unable to select serializer for $element")
      }
  }
}
