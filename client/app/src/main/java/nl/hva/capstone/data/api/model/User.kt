package nl.hva.capstone.data.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.hva.capstone.data.api.CapstoneApi

@Serializable
data class User (
  val id: Int,
  val username: String,
  val password: String,
  val avatar: String
) {
  val avatarURL get() = "${CapstoneApi.baseUrl}/cdn/proxy/${avatar}"
}
