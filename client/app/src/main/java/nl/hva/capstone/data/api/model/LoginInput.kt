package nl.hva.capstone.data.api.model

import kotlinx.serialization.Serializable

@Serializable
data class LoginInput (
  val username: String,
  val password: String
)
