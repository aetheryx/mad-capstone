package nl.hva.capstone.api.model.output

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OutgoingCallOffer(
  @SerialName("conversation_id") val conversationID: Int
)