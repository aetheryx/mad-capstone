package nl.hva.chatstone.ui.screens.conversation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

private val dayDividerFormatter = DateTimeFormatter.ofPattern("dd MMMM uuuu")

@Composable
fun DayDivider(timestamp: ZonedDateTime) {
  val formatted = dayDividerFormatter.format(timestamp)

  Box(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 8.dp)
  ) {
    Divider(
      modifier = Modifier.align(Alignment.Center),
    )
    Text(
      formatted,
      modifier = Modifier
        .align(Alignment.Center)
        .background(MaterialTheme.colorScheme.surface)
        .padding(horizontal = 4.dp),
      style = MaterialTheme.typography.titleSmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f),
      textAlign = TextAlign.Center
    )
  }
}