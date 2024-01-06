package nl.hva.chatstone.ui.screens.conversation

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FixedThreshold
import androidx.compose.material.SwipeableDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material.rememberSwipeableState
import androidx.compose.material.swipeable
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import nl.hva.chatstone.api.model.output.ConversationMessage
import nl.hva.chatstone.viewmodel.ConversationsViewModel
import nl.hva.chatstone.viewmodel.MessagesViewModel
import kotlin.math.roundToInt

@Composable
fun MessageReplyComposable(
  sourceMessage: ConversationMessage,
  conversationsVM: ConversationsViewModel
) {
  val messages = conversationsVM.messagesVM.messages[sourceMessage.conversationID]!!
  val message = messages.find { it.id == sourceMessage.replyToId!! } ?: return

  Row(
    modifier = Modifier
      .padding(horizontal = 8.dp)
      .padding(top = 8.dp)
      .background(Color.Black.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
      .padding(8.dp)
      .fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(4.dp)
  ) {
    Icon(
      Icons.Default.Reply,
      contentDescription = "Reply",
      modifier = Modifier.size(12.dp).scale(scaleX = -1f, scaleY = 1f)
    )

    Text(
      message.content,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
      style = MaterialTheme.typography.bodySmall
    )
  }
}

@OptIn(ExperimentalMaterialApi::class)
fun Modifier.buildSwipeModifier(
  messagesVM: MessagesViewModel,
  message: ConversationMessage,
  isAuthor: Boolean,
): Modifier = composed {
  if (isAuthor) {
    return@composed Modifier
  }

  val sizePx = LocalDensity.current.run { 64.dp.toPx() }
  val anchors = mapOf(0f to 0, sizePx to 1)
  val swipeableState = rememberSwipeableState(
    initialValue = 0,
    confirmStateChange = { state ->
      if (state == 1) {
        messagesVM.messageReply.value = message
      }

      false
    }
  )

  Modifier
    .swipeable(
      state = swipeableState,
      anchors = anchors,
      thresholds = { _, _ -> FixedThreshold(64.dp) },
      resistance = SwipeableDefaults.resistanceConfig(anchors.keys, 0f, 7.5f),
      orientation = Orientation.Horizontal
    )
    .offset { IntOffset(swipeableState.offset.value.roundToInt(), 0) }
}