package nl.hva.chatstone.ui.screens.home

import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import nl.hva.chatstone.R
import nl.hva.chatstone.ui.composables.DropdownActions
import nl.hva.chatstone.ui.theme.surfaceContainer
import nl.hva.chatstone.viewmodel.ConversationsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenTopAppBar(conversationsVM: ConversationsViewModel) {
  TopAppBar(
    colors = TopAppBarDefaults.topAppBarColors(
      containerColor = MaterialTheme.colorScheme.surfaceContainer
    ),
    title = { Text(stringResource(R.string.conversations)) },
    actions = { HomeScreenActions(conversationsVM) }
  )
}

@Composable
private fun HomeScreenActions(conversationsVM: ConversationsViewModel) {
  DropdownActions { unexpand ->
    DropdownMenuItem(
      onClick = {
        unexpand()
        conversationsVM.sessionVM.signOut()
      },
      text = { Text(stringResource(R.string.log_out)) }
    )
  }
}