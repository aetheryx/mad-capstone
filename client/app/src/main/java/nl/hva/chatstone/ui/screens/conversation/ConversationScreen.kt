package nl.hva.chatstone.ui.screens.conversation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import nl.hva.chatstone.api.model.output.Conversation
import nl.hva.chatstone.ui.components.UserProfilePicture
import nl.hva.chatstone.viewmodel.ConversationsViewModel

@Composable
fun ConversationScreen(
  navController: NavHostController,
  conversationID: Int,
  conversationsVM: ConversationsViewModel,
) {
  val conversations by conversationsVM.conversations.observeAsState(emptyList())
  val conversation = conversations.find { it.conversation.id == conversationID }.let { it ?: return }

  Scaffold(
    topBar = {
      ConversationScreenTopBar(navController, conversation, conversationsVM)
    },
    bottomBar = {
      MessageBar(conversationsVM, conversation)
    }
  ) {
    MessageList(conversationsVM, conversation, Modifier.padding(it))
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConversationScreenTopBar(
  navController: NavHostController,
  conversation: Conversation,
  conversationsVM: ConversationsViewModel
) {
  val user = conversation.otherParticipant
  val callVM = conversationsVM.sessionVM.callVM

  TopAppBar(
    colors = TopAppBarDefaults.topAppBarColors(),
    title = {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        UserProfilePicture(
          user,
          modifier = Modifier.size(48.dp)
        )

        Text(
          conversation.otherParticipant.username,
        )
      }
    },
    navigationIcon = {
      IconButton(
        onClick = navController::popBackStack
      ) {
        Icon(
          Icons.Default.ArrowBack,
          contentDescription = "Go back"
        )
      }
    },
    actions = {
      IconButton(onClick = {
        callVM.call(conversation)
      }) {
        Icon(Icons.Filled.Videocam, "Video call")
      }

      IconButton(onClick = {}) {
        Icon(Icons.Filled.MoreVert, "Details")
      }
    }
  )
}