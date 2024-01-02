package nl.hva.chatstone.api.model.output

import androidx.compose.runtime.Immutable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

@Serializable
@Immutable
data class ConversationMessage(
  val id: Int,
  @SerialName("author_id") val authorID: Int,
  @SerialName("conversation_id") val conversationID: Int,
  val content: String,
  @SerialName("created_at") val createdAtRaw: String
) {
  val createdAt: ZonedDateTime
    get() = Instant.parse("${createdAtRaw}Z")
      .atZone(ZoneId.systemDefault())
}
