package nl.hva.capstone.data.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.hva.capstone.data.api.model.User

@Serializable
data class FullConversation(
  val conversation: Conversation,
  @SerialName("other_participant") val otherParticipant: User
)
