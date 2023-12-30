package nl.hva.chatstone.api.model.input

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class IncomingCallOffer(
  @SerialName("callee_id") val calleeID: Int,
  @SerialName("conversation_id") val conversationID: Int,
)