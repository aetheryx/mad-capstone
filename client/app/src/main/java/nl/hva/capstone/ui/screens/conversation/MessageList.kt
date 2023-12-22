package nl.hva.capstone.ui.screens.conversation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import nl.hva.capstone.api.model.output.Conversation
import nl.hva.capstone.api.model.output.ConversationMessage
import nl.hva.capstone.viewmodel.ConversationsViewModel

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

  LazyColumn(modifier = modifier, reverseLayout = true, state = state) {
    items(
      items = messages,
      key = ConversationMessage::id
    ) { message ->
      MessageComponent(conversationsVM, message)
    }
  }
}

@Composable
private fun MessageComponent(
  conversationsVM: ConversationsViewModel,
  message: ConversationMessage
) {
  val isAuthor = message.authorID == conversationsVM.me.id

  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = if (isAuthor) Arrangement.End else Arrangement.Start,
  ) {
    Text("${message.id} ${message.content} ${message.createdAt}")
  }
}