package nl.hva.chatstone.api.model.output

import kotlinx.serialization.Serializable
import nl.hva.chatstone.api.ChatstoneApi

@Serializable
data class User (
  val id: Int,
  val username: String,
  val password: String,
  val avatar: String
) {
  val avatarURL get() = "${ChatstoneApi.BASE_URL}/cdn/proxy/${avatar}"
}
