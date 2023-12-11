package nl.hva.capstone.data.api.model

import kotlinx.serialization.Serializable

@Serializable
data class AuthResponse (
  val token: String
)
