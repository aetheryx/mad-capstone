package nl.hva.capstone.data.api.model

import kotlinx.serialization.Serializable

@Serializable
data class User (
  val id: Int,
  val username: String,
  val password: String
)
