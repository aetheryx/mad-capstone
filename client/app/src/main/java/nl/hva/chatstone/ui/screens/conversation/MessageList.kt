package nl.hva.chatstone.ui.screens.conversation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nl.hva.chatstone.api.model.output.Conversation
import nl.hva.chatstone.api.model.output.ConversationMessage
import nl.hva.chatstone.viewmodel.ConversationsViewModel

@Composable
fun MessageList(
  conversationsVM: ConversationsViewModel,
  conversation: Conversation,
  modifier: Modifier,
) {
  val messages = conversationsVM.messagesVM.messages[conversation.id]!!
  val state = remember { LazyListState() }

  LaunchedEffect(messages.size) {
    state.animateScrollToItem(0)
  }

  LazyColumn(
    modifier = modifier,
    reverseLayout = true,
    state = state,
    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
    verticalArrangement = Arrangement.spacedBy(4.dp)
  ) {
    itemsIndexed(
      items = messages as List<ConversationMessage>,
      key = { _, message -> message.id },
    ) { idx, message ->
      var messageModifier = Modifier.fillMaxWidth()

      if (idx > 0 && messages[idx - 1].authorID != message.authorID) {
        messageModifier = messageModifier.padding(bottom = 4.dp)
      }

      MessageComposable(conversationsVM, message, messageModifier)
    }
  }
}

