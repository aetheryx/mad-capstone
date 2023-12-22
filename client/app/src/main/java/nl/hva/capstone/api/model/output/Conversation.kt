package nl.hva.capstone.api.model.output

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Conversation(
  val conversation: RawConversation,
  @SerialName("other_participant") val otherParticipant: User,
  @SerialName("last_message") val lastMessage: ConversationMessage?,
) {
  val id get() = conversation.id
}
