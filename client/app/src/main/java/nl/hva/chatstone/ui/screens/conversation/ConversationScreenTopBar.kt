package nl.hva.chatstone.ui.screens.conversation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import nl.hva.chatstone.R
import nl.hva.chatstone.api.model.output.Conversation
import nl.hva.chatstone.ui.composables.UserProfilePicture
import nl.hva.chatstone.ui.theme.surfaceContainer
import nl.hva.chatstone.viewmodel.ConversationsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationScreenTopBar(
  navController: NavHostController,
  conversation: Conversation,
  conversationsVM: ConversationsViewModel
) {
  val user = conversation.otherParticipant
  val callVM = conversationsVM.sessionVM.callVM

  TopAppBar(
    colors = TopAppBarDefaults.topAppBarColors(
      containerColor = MaterialTheme.colorScheme.surfaceContainer
    ),
    title = {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        UserProfilePicture(
          user,
          modifier = Modifier.size(48.dp)
        )

        Text(
          conversation.otherParticipant.username,
          textAlign = TextAlign.Center
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
        callVM.launchActivity()
      }) {
        Icon(Icons.Filled.Videocam, "Video call")
      }

      ConversationScreenDetails(conversationsVM, conversation)
    }
  )
}

@Composable
private fun ConversationScreenDetails(
  conversationsVM: ConversationsViewModel,
  conversation: Conversation
) {
  var expanded by remember { mutableStateOf(false) }

  IconButton(onClick = { expanded = !expanded }) {
    Icon(Icons.Filled.MoreVert, "Details")
  }

  DropdownMenu(
    expanded = expanded,
    onDismissRequest = { expanded = !expanded }
  ) {
    DropdownMenuItem(
      text = { Text(stringResource(R.string.delete_conversation)) },
      onClick = {
        conversationsVM.deleteConversation(conversation)
      }
    )
  }
}