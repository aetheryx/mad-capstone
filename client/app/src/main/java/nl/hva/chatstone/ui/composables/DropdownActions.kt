package nl.hva.chatstone.ui.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import nl.hva.chatstone.R

@Composable
fun DropdownActions(
  content: @Composable ColumnScope.(() -> Unit) -> Unit
) {
  var expanded by remember { mutableStateOf(false) }

  IconButton(onClick = { expanded = !expanded }) {
    Icon(Icons.Filled.MoreVert, stringResource(R.string.more))
  }

  DropdownMenu(
    expanded = expanded,
    onDismissRequest = { expanded = !expanded },
    content = {
      val unexpand = { expanded = false }
      content(unexpand)
    },
    modifier = Modifier.clickable {
      expanded = !expanded
    }
  )
}