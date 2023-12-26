package nl.hva.capstone.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CallResponse(
  @SerialName("caller_id") val callerID: Int,
  val accepted: Boolean
)