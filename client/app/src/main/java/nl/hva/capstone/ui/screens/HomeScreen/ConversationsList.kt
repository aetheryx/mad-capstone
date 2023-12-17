package nl.hva.capstone.ui.screens.HomeScreen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import nl.hva.capstone.R
import nl.hva.capstone.data.api.FullConversation
import nl.hva.capstone.data.api.model.User
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

  val model = ImageRequest.Builder(LocalContext.current)
    .data(user.avatarURL)
    .fallback(R.drawable.default_pfp)
    .build()

  Row() {
    AsyncImage(
      model,
      contentDescription = "${user.username}'s profile picture",
      contentScale = ContentScale.Crop,
      modifier = Modifier
        .size(64.dp)
        .clip(CircleShape)
    )

    Column() {
      ConversationDetails(conversation, user)
    }
  }
}

@Composable
private fun ConversationDetails(conversation: FullConversation, user: User) {
  Row() {
    Text(user.username)

    if (conversation.lastMessage != null) {
      Text(conversation.lastMessage.createdAt)
    }
  }

  if (conversation.lastMessage != null) {
    Text(conversation.lastMessage.content)
  }
}

