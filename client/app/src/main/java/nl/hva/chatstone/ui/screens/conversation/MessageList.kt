package nl.hva.chatstone.ui.screens.conversation

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import nl.hva.chatstone.R
import nl.hva.chatstone.api.model.output.Conversation
import nl.hva.chatstone.api.model.output.ConversationMessage
import nl.hva.chatstone.viewmodel.ConversationsViewModel
import java.time.format.DateTimeFormatter
import kotlin.math.max
import kotlin.math.roundToInt

@Composable
fun MessageList(
  conversationsVM: ConversationsViewModel,
  conversation: Conversation,
  modifier: Modifier,
) {
  val messages = conversationsVM.messagesVM.messages[conversation.id]!!
  val state = remember { LazyListState() }

  LaunchedEffect(messages.size) {
    state.animateScrollToItem(0)
  }

  LazyColumn(
    modifier = modifier,
    reverseLayout = true,
    state = state,
    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
    verticalArrangement = Arrangement.spacedBy(4.dp)
  ) {
    itemsIndexed(
      items = messages as List<ConversationMessage>,
      key = { _, message -> message.id },
    ) { idx, message ->
      var messageModifier = Modifier.fillMaxWidth()

      if (idx > 0 && messages[idx - 1].authorID != message.authorID) {
        messageModifier = messageModifier.padding(bottom = 4.dp)
      }

      MessageComponent(conversationsVM, message, messageModifier)
    }
  }
}

val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun MessageComponent(
  conversationsVM: ConversationsViewModel,
  message: ConversationMessage,
  modifier: Modifier
) {
  val isAuthor = message.authorID == conversationsVM.me.id
  val messagesVM = conversationsVM.messagesVM

  val arrangement = Arrangement.let { if (isAuthor) it.End else it.Start }
  val color = MaterialTheme.colorScheme.let {
    if (isAuthor) it.primaryContainer else it.outlineVariant
  }

  var outerPadding by remember { mutableIntStateOf(0) }
  var timestampPadding by remember { mutableStateOf(0.dp) }

  val outerDp = LocalDensity.current.run { outerPadding.toDp() }

  val swipeModifier = if (!isAuthor) {
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
  } else {
    Modifier
  }

  Row(
    horizontalArrangement = arrangement,
    modifier = modifier
      .fillMaxWidth()
      .then(swipeModifier),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(0.8f),
      horizontalArrangement = arrangement,
    ) {
      Column(
        modifier = Modifier
          .then(
            if (isAuthor) Modifier.padding(start = outerDp)
            else Modifier.padding(end = outerDp)
          )
          .clip(RoundedCornerShape(8.dp))
          .background(color)
          .width(IntrinsicSize.Max),
      ) {
        if (message.replyToId != null) {
          MessageReply(message, conversationsVM)
        }

        Box(
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(
            message.content,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier
              .align(Alignment.TopStart)
              .padding(8.dp)
              .padding(end = timestampPadding),
            onTextLayout = { layout ->
              if (layout.lineCount == 1 && timestampPadding == 0.dp) {
                timestampPadding = 36.dp
              } else if (outerPadding == 0) {
                val deltaPx = (0 until layout.lineCount)
                  .map {
                    var right = layout.getLineRight(it) * 1.025f
                    if (it == (layout.lineCount - 1)) right *= 1.2f
                    layout.size.width - right
                  }
                  .minOf { max(it, 0.0f) }
                  .toInt()

                outerPadding = deltaPx
              }
            }
          )

          Text(
            dateTimeFormatter.format(message.createdAt),
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier
              .alpha(0.7f)
              .padding(start = 4.dp, end = 6.dp, bottom = 6.dp)
              .align(Alignment.BottomEnd),
          )
        }
      }
    }
  }
}

@Composable
private fun MessageReply(
  sourceMessage: ConversationMessage,
  conversationsVM: ConversationsViewModel
) {
  val messages = conversationsVM.messagesVM.messages[sourceMessage.conversationID]!!
  val message = messages.find { it.id == sourceMessage.replyToId!! }!!
  val author = if (message.authorID == conversationsVM.me.id) {
    stringResource(R.string.you)
  } else {
    val conversation = conversationsVM.getConversation(message.conversationID)!!
    conversation.otherParticipant.username
  }

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
