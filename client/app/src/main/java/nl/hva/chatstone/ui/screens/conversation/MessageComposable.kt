package nl.hva.chatstone.ui.screens.conversation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.dp
import nl.hva.chatstone.api.model.output.ConversationMessage
import java.time.format.DateTimeFormatter
import kotlin.math.max

private val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

@Composable
fun MessageComposable(
  message: ConversationMessage,
  modifier: Modifier,
  onReply: () -> Unit
) {
  Box(modifier.fillMaxWidth()) {
    val innerModifier = Modifier
      .align(
        if (message.isAuthor) Alignment.TopEnd else Alignment.TopStart
      )
      .then(
        64.dp.let {
          if (message.isAuthor) Modifier.padding(start = it)
          else Modifier.padding(end = it)
        }
      )

    MessageInner(
      message,
      innerModifier,
      onReply
    )
  }
}

@Composable
private fun MessageInner(
  message: ConversationMessage,
  modifier: Modifier,
  onReply: () -> Unit
) {
  val color = MaterialTheme.colorScheme.let {
    if (message.isAuthor) it.primaryContainer else it.outlineVariant
  }

  val outerPadding = remember { mutableIntStateOf(0) }
  val outerDp = LocalDensity.current.run { outerPadding.intValue.toDp() }
  val swipeModifier = Modifier.buildSwipeModifier(
    message = message,
    onSwipe = onReply
  )

  Column(
    modifier = modifier
      .then(swipeModifier)
      .then(
        if (message.isAuthor) Modifier.padding(start = outerDp)
        else Modifier.padding(end = outerDp)
      )
      .clip(RoundedCornerShape(8.dp))
      .background(color)
      .width(IntrinsicSize.Max),
  ) {
    message.replyTo?.let {
      MessageReplyComposable(it)
    }

    MessageInnerBox(
      message,
      outerPadding
    )
  }
}

@Composable
private fun MessageInnerBox(
  message: ConversationMessage,
  outerPadding: MutableIntState
) {
  var timestampEndPadding by remember { mutableStateOf(0.dp) }
  var timestampBottomPadding by remember { mutableStateOf(0.dp) }
  val timestampWidth = LocalDensity.current.run { (30 + 4).dp.toPx() }
  
  val onTextLayout = fun (layout: TextLayoutResult) {
    if (layout.size.width == 0) return

    if (layout.lineCount == 1 && timestampEndPadding == 0.dp && timestampBottomPadding == 0.dp) {
      timestampEndPadding = 36.dp
    } else if (layout.lineCount != 1 && timestampEndPadding != 0.dp) {
      timestampBottomPadding = 16.dp
      timestampEndPadding = 0.dp
    } else if (outerPadding.intValue == 0 && timestampEndPadding == 0.dp) {
      val deltaPx = (0 until layout.lineCount)
        .map {
          var right = layout.getLineRight(it) * 1.025f
          if (it == (layout.lineCount - 1)) right += timestampWidth
          layout.size.width - right
        }
        .minOf { max(it, 0.0f) }
        .toInt()

      outerPadding.intValue = deltaPx
    }

    if (layout.lineCount != 1 && timestampEndPadding == 0.dp) {
      val right = layout.getLineRight(layout.lineCount - 1)
      val diff = layout.size.width - right
      if (diff < timestampWidth) {
        timestampBottomPadding = 16.dp
      }
    }
  }

  Box(Modifier.fillMaxWidth()) {
    Text(
      message.content,
      style = MaterialTheme.typography.bodyLarge,
      modifier = Modifier
        .align(Alignment.TopStart)
        .padding(8.dp)
        .padding(end = timestampEndPadding)
        .padding(bottom = timestampBottomPadding),
      onTextLayout = onTextLayout
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

