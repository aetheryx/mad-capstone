package nl.hva.capstone.api.model.input

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateConversationInput(
  @SerialName("other_user") val otherUser: Int
)
