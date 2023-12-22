package nl.hva.capstone.api.model.output

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ConversationMessage(
  val id: Int,
  @SerialName("author_id") val authorID: Int,
  @SerialName("conversation_id") val conversationID: Int,
  val content: String,
  @SerialName("created_at") val createdAt: String
)
