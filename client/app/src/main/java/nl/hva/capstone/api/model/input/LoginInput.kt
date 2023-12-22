package nl.hva.capstone.api.model.input

import kotlinx.serialization.Serializable

@Serializable
data class LoginInput (
  val username: String,
  val password: String
)
