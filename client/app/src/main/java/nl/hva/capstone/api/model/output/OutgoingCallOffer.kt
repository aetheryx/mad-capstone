package nl.hva.capstone.api.model.output

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OutgoingCallOffer(
  val callee: User,
  @SerialName("conversation_id") val conversationID: Int
)