package nl.hva.chatstone.ui.screens.conversation

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import nl.hva.chatstone.api.model.output.Conversation
import nl.hva.chatstone.ui.theme.surfaceContainer
import nl.hva.chatstone.viewmodel.ConversationsViewModel

@Composable
fun MessageBar(
  conversationsVM: ConversationsViewModel,
  conversation: Conversation
) {
  Column() {
    ReplyBar(conversationsVM)
    Divider()
    MessageBarTextField(conversationsVM, conversation)
  }
}

@Composable
private fun ReplyBar(conversationsVM: ConversationsViewModel) {
  val messagesVM = conversationsVM.messagesVM
  var replyText by remember { mutableStateOf("") }

  val reply by messagesVM.messageReply.observeAsState()
  LaunchedEffect(reply) {
    if (reply != null) {
      replyText = reply!!.content
    }
  }

  val height by animateDpAsState(
    reply?.let { 40.dp } ?: 0.dp,
    label = "Reply"
  )

  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(16.dp),
    modifier = Modifier
      .fillMaxWidth()
      .height(height)
      .background(MaterialTheme.colorScheme.surfaceContainer)
      .padding(horizontal = 16.dp),
  ) {
    IconButton(
      onClick = {
        messagesVM.messageReply.value = null
      },
      colors = IconButtonDefaults.filledIconButtonColors(
        containerColor = MaterialTheme.colorScheme.inverseSurface
      ),
      modifier = Modifier.size(24.dp).alpha(0.6f)
    ) {
      Icon(
        Icons.Filled.Close,
        contentDescription = "Cancel reply",
        modifier = Modifier.size(12.dp)
      )
    }

    Row() {
      Text(
        "Replying to ",
        modifier = Modifier.alignByBaseline(),
        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
        color = MaterialTheme.colorScheme.onSurface
      )
      Text(
        replyText,
        overflow = TextOverflow.Ellipsis,
        maxLines = 1,
        modifier = Modifier.alignByBaseline(),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface
      )
    }

  }
}

@Composable
private fun MessageBarTextField(
  conversationsVM: ConversationsViewModel,
  conversation: Conversation
) {
  val messagesVM = conversationsVM.messagesVM
  var messageContent by remember { mutableStateOf("") }
  val reconnecting by conversationsVM.sessionVM.websocket.reconnecting.observeAsState(false)

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .background(MaterialTheme.colorScheme.surfaceContainer)
      .navigationBarsPadding()
      .imePadding()
      .padding(16.dp)
      .height(48.dp)
      .clip(RoundedCornerShape(50))
      .background(MaterialTheme.colorScheme.surfaceVariant),
    verticalAlignment = Alignment.CenterVertically
  ) {
    BasicTextField(
      messageContent,
      onValueChange = { messageContent = it },
      enabled = !reconnecting,
      textStyle = LocalTextStyle.current.copy(color = MaterialTheme.colorScheme.onSurface),
      cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
      modifier = Modifier
        .weight(1f)
        .padding(horizontal = 16.dp)
    )

    val enabled = messageContent.isNotBlank() && !reconnecting

    IconButton(
      modifier = Modifier
        .padding(end = 4.dp)
        .alpha(if (enabled) 1.0f else 0.38f),
      enabled = enabled,
      onClick = {
        messagesVM.sendMessage(conversation, messageContent)
        messageContent = ""
      }
    ) {
      Icon(
        Icons.Filled.Send,
        contentDescription = "Send",
        tint = MaterialTheme.colorScheme.onSurface
      )
    }
  }
}

