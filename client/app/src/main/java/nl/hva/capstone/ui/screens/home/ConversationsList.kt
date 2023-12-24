package nl.hva.capstone.ui.screens.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import nl.hva.capstone.api.model.output.Conversation
import nl.hva.capstone.api.model.output.User
import nl.hva.capstone.ui.components.UserProfilePicture
import nl.hva.capstone.viewmodel.ConversationsViewModel

@Composable
fun ConversationsList(
  navController: NavHostController,
  conversationsVM: ConversationsViewModel,
  modifier: Modifier
) {
  val conversations by conversationsVM.conversations.observeAsState(emptyList())

  Column(
    modifier = modifier
  ) {
    LazyColumn(
      contentPadding = PaddingValues(horizontal = 16.dp),
      verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
      items(conversations) {
        Conversation(navController, it)
      }
    }
  }
}

@Composable
private fun Conversation(navController: NavHostController, conversation: Conversation) {
  val user = conversation.otherParticipant

  Row(
    modifier = Modifier
      .height(56.dp)
      .fillMaxWidth()
      .clickable {
        navController.navigate("/conversations/${conversation.conversation.id}")
      },
    horizontalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    UserProfilePicture(
      user,
      modifier = Modifier
        .fillMaxHeight()
        .width(56.dp)
    )

    ConversationDetails(conversation, user)
  }
}

@Composable
private fun ConversationDetails(conversation: Conversation, user: User) {
  Column(
    verticalArrangement = Arrangement.Center,
    modifier = Modifier.fillMaxHeight()
  ) {
    Row(
      modifier = Modifier.weight(0.55f).fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Text(
        user.username,
        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
      )

      conversation.lastMessage?.let {
        Text(
          conversation.lastMessage.createdAt,
        )
      }
    }

    Row(
      modifier = Modifier.weight(0.45f).fillMaxWidth(),
    ) {
      Text(
        conversation.lastMessage?.content ?: "Say something!",
        style = MaterialTheme.typography.bodyMedium,
      )
    }
  }
}

