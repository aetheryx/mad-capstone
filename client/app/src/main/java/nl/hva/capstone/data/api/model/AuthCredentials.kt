package nl.hva.capstone.data.api.model

import kotlinx.serialization.Serializable

@Serializable
data class AuthCredentials (
  val username: String,
  val password: String
)
