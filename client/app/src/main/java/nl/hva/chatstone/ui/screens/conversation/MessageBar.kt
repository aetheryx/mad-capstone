package nl.hva.chatstone.ui.screens.conversation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.IconButton
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import nl.hva.chatstone.api.model.output.Conversation
import nl.hva.chatstone.ui.theme.surfaceContainer
import nl.hva.chatstone.viewmodel.ConversationsViewModel


@Composable
fun MessageBar(
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

