package nl.hva.chatstone.ui.screens.home

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import nl.hva.chatstone.R
import nl.hva.chatstone.ui.composables.ChatstoneSnackbarHost
import nl.hva.chatstone.ui.theme.surfaceContainer
import nl.hva.chatstone.viewmodel.ConversationsViewModel

@Composable
fun HomeScreen(
  navController: NavHostController,
  conversationsVM: ConversationsViewModel,
  modifier: Modifier
) {
  Scaffold(
    topBar = {
      HomeScreenTopAppBar(conversationsVM)
    },
    snackbarHost = {
      ChatstoneSnackbarHost(conversationsVM)
    },
    floatingActionButton = {
      FloatingActionButton(
        onClick = {
          navController.navigate("/conversations/add")
        }
      ) {
        Icon(
          Icons.Filled.Add,
          contentDescription = "Add"
        )
      }
    },
    modifier = modifier
  ) {
    ConversationsList(navController, conversationsVM, Modifier.padding(top = it.calculateTopPadding()))
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreenTopAppBar(conversationsVM: ConversationsViewModel) {
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
  var expanded by remember { mutableStateOf(false) }

  IconButton(onClick = { expanded = !expanded }) {
    Icon(Icons.Filled.MoreVert, stringResource(R.string.more))
  }

  DropdownMenu(
    expanded = expanded,
    onDismissRequest = { expanded = !expanded }
  ) {
    DropdownMenuItem(
      onClick = {
        expanded = false
        conversationsVM.sessionVM.signOut()
      },
      text = { Text(stringResource(R.string.log_out)) }
    )

    DropdownMenuItem(
      onClick = {},
      text = { Text(stringResource(R.string.edit_profile)) }
    )
  }
}