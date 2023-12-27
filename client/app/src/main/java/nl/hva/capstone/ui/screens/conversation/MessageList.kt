package nl.hva.capstone.ui.screens.conversation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import nl.hva.capstone.api.model.output.Conversation
import nl.hva.capstone.api.model.output.ConversationMessage
import nl.hva.capstone.viewmodel.ConversationsViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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

  val horizontalArrangement = Arrangement.let { if (isAuthor) it.End else it.Start }
  val color = MaterialTheme.colorScheme.let {
    if (isAuthor) it.primaryContainer else it.secondaryContainer
  }

  Row(
    horizontalArrangement = horizontalArrangement,
    modifier = Modifier.fillMaxWidth(),
  ) {
    Row(
      verticalAlignment = Alignment.Bottom,
      modifier = Modifier
        .clip(RoundedCornerShape(25))
        .background(color)
    ) {
      Text(
        message.content,
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier.padding(start = 10.dp, top = 8.dp, bottom = 8.dp)
      )

      val timestamp = LocalDateTime.parse(message.createdAt)
      Text(
        dateTimeFormatter.format(timestamp),
        style = MaterialTheme.typography.labelSmall,
        modifier = Modifier
          .alpha(0.7f)
          .padding(start = 4.dp, end = 6.dp, bottom = 6.dp)
      )
    }
  }
}