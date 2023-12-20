package nl.hva.capstone.ui.screens.ConversationScreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import nl.hva.capstone.data.api.FullConversation
import nl.hva.capstone.data.api.model.ConversationMessage
import nl.hva.capstone.viewmodel.ConversationsViewModel

@Composable
fun MessageList(
  conversationsViewModel: ConversationsViewModel,
  conversation: FullConversation
) {
  val liveData = conversationsViewModel.conversationMessages[conversation.conversation.id]!!
  val messages by liveData.observeAsState(emptyList())

  LazyColumn() {
    items(messages.reversed()) { message ->
      MessageComponent(conversationsViewModel, message)
    }
  }
}

@Composable
private fun MessageComponent(
  conversationsViewModel: ConversationsViewModel,
  message: ConversationMessage
) {
  val isAuthor = message.authorID == conversationsViewModel.me.id

  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = if (isAuthor) Arrangement.End else Arrangement.Start,
  ) {
    Text("${message.content} ${message.createdAt}")
  }
}