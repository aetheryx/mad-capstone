package nl.hva.capstone.api

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import nl.hva.capstone.api.model.output.ConversationMessage

@Serializable
sealed class WebsocketEvent {
  @Serializable
  data class MessageCreate(val data: ConversationMessage) : WebsocketEvent()

  companion object : JsonContentPolymorphicSerializer<WebsocketEvent>(WebsocketEvent::class) {
    override fun selectDeserializer(element: JsonElement) =
      when (element.jsonObject["event"]?.jsonPrimitive?.content) {
        "MessageCreate" -> MessageCreate.serializer()
        else -> error("unable to select serializer for $element")
      }
  }
}
