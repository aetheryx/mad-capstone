package nl.hva.capstone.api.model.input

import kotlinx.serialization.Serializable

@Serializable
data class CreateMessageInput (
  val content: String
)
