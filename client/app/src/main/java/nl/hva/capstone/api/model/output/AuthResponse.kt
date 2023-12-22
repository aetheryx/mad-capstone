package nl.hva.capstone.api.model.output

import kotlinx.serialization.Serializable

@Serializable
data class AuthResponse (
  val token: String
)
