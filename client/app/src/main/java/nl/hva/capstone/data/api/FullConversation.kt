package nl.hva.capstone.data.api

import kotlinx.serialization.Serializable
import nl.hva.capstone.data.api.model.User

@Serializable
data class FullConversation(
  val conversation: Conversation,
  val participants: List<User>
)
