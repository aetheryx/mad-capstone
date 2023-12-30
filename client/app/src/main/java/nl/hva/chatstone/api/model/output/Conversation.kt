package nl.hva.chatstone.api.model.output

import androidx.compose.runtime.Immutable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@Immutable
data class Conversation(
  val conversation: RawConversation,
  @SerialName("other_participant") val otherParticipant: User,
  @SerialName("last_message") val lastMessage: ConversationMessage?,
) {
  val id get() = conversation.id
}
