package nl.hva.capstone.api.model.output

import kotlinx.serialization.Serializable
import nl.hva.capstone.api.CapstoneApi

@Serializable
data class User (
  val id: Int,
  val username: String,
  val password: String,
  val avatar: String
) {
  val avatarURL get() = "${CapstoneApi.BASE_URL}/cdn/proxy/${avatar}"
}
