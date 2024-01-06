package nl.hva.chatstone.api.model.input

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateMessageInput (
  val content: String,
  @SerialName("reply_to_id") val replyToId: Int?,
)
