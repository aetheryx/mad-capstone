package nl.hva.chatstone.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CallResponse(
  @SerialName("caller_id") val callerID: Int,
  @SerialName("call_id") val callID: Int,
  val accepted: Boolean
)