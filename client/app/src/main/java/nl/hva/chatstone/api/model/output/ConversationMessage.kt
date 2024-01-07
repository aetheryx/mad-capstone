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
  @SerialName("created_at") val createdAtRaw: String,
  @SerialName("reply_to_id") val replyToId: Int?,
) {
  var isAuthor: Boolean = false
  var replyTo: ConversationMessage? = null

  val createdAt: ZonedDateTime
    get() = Instant.parse("${createdAtRaw}Z")
      .atZone(ZoneId.systemDefault())
}
