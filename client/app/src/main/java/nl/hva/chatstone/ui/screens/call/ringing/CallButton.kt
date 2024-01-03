package nl.hva.chatstone.ui.screens.call.ringing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun CallButton(
  containerColor: Color,
  onClick: () -> Unit,
  label: String? = null,
  icon: ImageVector
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    IconButton(
      modifier = Modifier.size(80.dp),
      colors = IconButtonDefaults.filledIconButtonColors(
        containerColor = containerColor
      ),
      onClick = onClick,
    ) {
      Icon(
        icon,
        contentDescription = label ?: icon.name,
        modifier = Modifier.size(48.dp)
      )
    }

    if (label != null) {
      Text(
        label,
        style = MaterialTheme.typography.labelLarge,
        modifier = Modifier.alpha(0.62f)
      )
    }
  }
}