package nl.hva.chatstone.api.model.input

import kotlinx.serialization.Serializable

@Serializable
data class SignupInput(
  val username: String,
  val password: String,
  val avatar: String
)
