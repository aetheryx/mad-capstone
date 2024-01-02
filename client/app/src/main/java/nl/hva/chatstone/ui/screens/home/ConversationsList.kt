package nl.hva.chatstone.ui.screens.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import nl.hva.chatstone.R
import nl.hva.chatstone.api.model.output.Conversation
import nl.hva.chatstone.api.model.output.User
import nl.hva.chatstone.ui.composables.UserProfilePicture
import nl.hva.chatstone.viewmodel.ConversationsViewModel
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

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
    LazyColumn() {
      items(
        items = conversations,
        key = Conversation::id
      ) {
        Conversation(navController, it)
      }
    }
  }
}

@Composable
private fun Conversation(navController: NavHostController, conversation: Conversation) {
  val user = conversation.otherParticipant

  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier
      .height(80.dp)
      .clickable {
        navController.navigate("/conversations/${conversation.id}")
      },
  ) {
    Row(
      horizontalArrangement = Arrangement.spacedBy(16.dp),
      modifier = Modifier
        .fillMaxWidth()
        .height(56.dp)
        .padding(horizontal = 16.dp)
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
}

@Composable
private fun ConversationDetails(conversation: Conversation, user: User) {
  Column(
    verticalArrangement = Arrangement.Center,
    modifier = Modifier.fillMaxHeight()
  ) {
    Row(
      modifier = Modifier
        .weight(0.55f)
        .fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Text(
        user.username,
        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
      )

      Text(
        conversationDate(conversation.lastMessage?.createdAt),
        style = MaterialTheme.typography.labelMedium,
        modifier = Modifier.alpha(0.7f)
      )
    }

    Row(
      modifier = Modifier
        .weight(0.45f)
        .fillMaxWidth(),
    ) {
      Text(
        conversation.lastMessage?.content ?: "Say something!",
        style = MaterialTheme.typography.bodyMedium,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.alpha(0.7f)
      )
    }
  }
}

val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yy")

@Composable
private fun conversationDate( date: ZonedDateTime?): String {
  if (date == null) return stringResource(R.string.never)

  val today = ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS)
  if (today.isBefore(date)) {
    return timeFormatter.format(date)
  }

  val yesterday = today.minusDays(1)
  if (yesterday.isBefore(date)) {
    return stringResource(R.string.yesterday)
  }

  return dateFormatter.format(date)
}