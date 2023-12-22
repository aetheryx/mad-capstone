package nl.hva.capstone.ui.screens.conversation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import nl.hva.capstone.api.model.output.Conversation
import nl.hva.capstone.viewmodel.ConversationsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationScreen(
  conversationID: Int,
  conversationsVM: ConversationsViewModel,
) {
  val conversations by conversationsVM.conversations.observeAsState(emptyList())
  val conversation = conversations.find { it.conversation.id == conversationID }!!

  Scaffold(
    topBar = {
      TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(),
        title = {
          val model = ImageRequest.Builder(LocalContext.current)
            .data(conversation.otherParticipant.avatarURL)
            .fallback(R.drawable.default_pfp)
            .build()

          Row() {
            AsyncImage(
              model,
              contentDescription = "${conversation.otherParticipant.username}'s profile picture",
              contentScale = ContentScale.Crop,
              modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
            )

            Text(conversation.otherParticipant.username)
          }
        },
        navigationIcon = {
          Icon(
            Icons.Default.ArrowBack,
            contentDescription = "Go back"
          )
        },
        actions = {
          Icon(Icons.Filled.Videocam, "Video call")
          Icon(Icons.Filled.MoreVert, "Details")
        }
      )
    }
  ) {
    ConversationsView(conversationsVM, conversation, Modifier.padding(it))
  }
}

@Composable
private fun ConversationsView(
  conversationsVM: ConversationsViewModel,
  conversation: Conversation,
  modifier: Modifier
) {
  Column(
    modifier = modifier.fillMaxSize(),
    verticalArrangement = Arrangement.SpaceBetween
  ) {
    MessageList(conversationsVM, conversation)
    MessageBar(conversationsVM, conversation)
  }
}