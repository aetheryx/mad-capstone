package nl.hva.chatstone.api.model.input

import kotlinx.serialization.Serializable

@Serializable
data class CreateMessageInput (
  val content: String
)
