package nl.hva.chatstone.ui.screens.conversation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import nl.hva.chatstone.api.model.output.Conversation
import nl.hva.chatstone.ui.composables.ChatstoneSnackbarHost
import nl.hva.chatstone.viewmodel.ConversationsViewModel

@Composable
fun ConversationScreen(
  navController: NavHostController,
  conversationsVM: ConversationsViewModel,
  conversation: Conversation
) {
  Scaffold(
    topBar = {
      ConversationScreenTopBar(navController, conversation, conversationsVM)
    },
    bottomBar = {
      MessageBar(conversationsVM, conversation)
    },
    snackbarHost = {
      ChatstoneSnackbarHost(conversationsVM)
    }
  ) {
    MessageList(conversationsVM, conversation, Modifier.padding(it))
  }
}

