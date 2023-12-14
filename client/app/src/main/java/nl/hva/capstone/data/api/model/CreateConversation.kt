package nl.hva.capstone.data.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateConversation(
  @SerialName("other_user") val otherUser: Int
)
