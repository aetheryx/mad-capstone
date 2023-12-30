package nl.hva.chatstone.ui.screens.conversation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import nl.hva.chatstone.api.model.output.Conversation
import nl.hva.chatstone.api.model.output.ConversationMessage
import nl.hva.chatstone.viewmodel.ConversationsViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.max

@Composable
fun MessageList(
  conversationsVM: ConversationsViewModel,
  conversation: Conversation,
  modifier: Modifier,
) {
  val messages = conversationsVM.conversationMessages[conversation.id]!!
  val state = remember { LazyListState() }

  LaunchedEffect(messages.size) {
    state.animateScrollToItem(0)
  }

  LazyColumn(
    modifier = modifier.padding(bottom = 4.dp),
    reverseLayout = true,
    state = state,
    contentPadding = PaddingValues(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(4.dp)
  ) {
    items(
      items = messages,
      key = ConversationMessage::id
    ) { message ->
      MessageComponent(conversationsVM, message)
    }
  }
}

val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

@Composable
private fun MessageComponent(
  conversationsVM: ConversationsViewModel,
  message: ConversationMessage
) {
  val isAuthor = message.authorID == conversationsVM.me.id

  val arrangement = Arrangement.let { if (isAuthor) it.End else it.Start }
  val color = MaterialTheme.colorScheme.let {
    if (isAuthor) it.primaryContainer else it.secondaryContainer
  }

  var outerPadding by remember { mutableIntStateOf(0) }
  var timestampPadding by remember { mutableStateOf(0.dp) }

  val outerDp = LocalDensity.current.run { outerPadding.toDp() }

  Row(
    horizontalArrangement = arrangement,
    modifier = Modifier.fillMaxWidth(),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(0.8f),
      horizontalArrangement = arrangement,
    ) {
      Box(
        modifier = Modifier
          .then(
            if (isAuthor) Modifier.padding(start = outerDp)
            else Modifier.padding(end = outerDp)
          )
          .clip(RoundedCornerShape(8.dp))
          .background(color)
      ) {
        Text(
          message.content,
          style = MaterialTheme.typography.bodyLarge,
          modifier = Modifier
            .align(Alignment.TopStart)
            .padding(8.dp)
            .padding(end = timestampPadding),
          onTextLayout = { layout ->
            if (layout.lineCount == 1 && timestampPadding == 0.dp) {
              timestampPadding = 36.dp
            } else if (outerPadding == 0) {
              val deltaPx = (0 until layout.lineCount)
                .map {
                  var right = layout.getLineRight(it) * 1.025f
                  if (it == (layout.lineCount - 1)) right *= 1.2f
                  layout.size.width - right
                }
                .minOf { max(it, 0.0f) }
                .toInt()

              outerPadding = deltaPx
            }
          }
        )

        val timestamp = LocalDateTime.parse(message.createdAt)
        Text(
          dateTimeFormatter.format(timestamp),
          style = MaterialTheme.typography.labelSmall,
          modifier = Modifier
            .alpha(0.7f)
            .padding(start = 4.dp, end = 6.dp, bottom = 6.dp)
            .align(Alignment.BottomEnd),
        )
      }
    }
  }
}