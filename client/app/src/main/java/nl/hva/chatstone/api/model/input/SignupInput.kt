package nl.hva.chatstone.api.model.input

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SignupInput(
  val username: String,
  val password: String,
  @SerialName("profile_picture_url") val profilePictureURL: String
)
