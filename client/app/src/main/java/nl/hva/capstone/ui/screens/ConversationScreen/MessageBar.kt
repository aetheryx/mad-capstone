package nl.hva.capstone.ui.screens.ConversationScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.IconButton
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.primarySurface
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import nl.hva.capstone.data.api.FullConversation
import nl.hva.capstone.viewmodel.ConversationsViewModel

@Composable
fun MessageBar(
  conversationsViewModel: ConversationsViewModel,
  conversation: FullConversation
) {
  var messageContent by remember { mutableStateOf("") }

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .height(48.dp)
      .clip(RoundedCornerShape(50))
      .background(MaterialTheme.colors.primarySurface),
    verticalAlignment = Alignment.CenterVertically
  ) {
    BasicTextField(
      messageContent,
      onValueChange = { messageContent = it },
      textStyle = LocalTextStyle.current.copy(color = MaterialTheme.colors.onPrimary),
      cursorBrush = SolidColor(MaterialTheme.colors.onPrimary),
      singleLine = true,
      modifier = Modifier.weight(1f).padding(horizontal = 16.dp)
    )

    IconButton(
      onClick = {
        conversationsViewModel.sendMessage(conversation.conversation, messageContent)
        messageContent = ""
      }
    ) {
      Icon(
        Icons.Filled.Send,
        contentDescription = "Send",
        tint = MaterialTheme.colors.onPrimary
      )
    }
  }
}

