package nl.hva.capstone.ui.screens.HomeScreen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import nl.hva.capstone.data.api.FullConversation
import nl.hva.capstone.viewmodel.ConversationsViewModel

@Composable
fun ConversationsList(
  conversationsViewModel: ConversationsViewModel,
  modifier: Modifier
) {
  val conversations by conversationsViewModel.conversations.observeAsState(emptyList())

  Column(
    modifier = modifier
  ) {
    LazyColumn {
      items(conversations) {
        Conversation(it)
      }
    }
  }
}

@Composable
private fun Conversation(conversation: FullConversation) {
  val user = conversation.otherParticipant
  Text("$conversation")
}

