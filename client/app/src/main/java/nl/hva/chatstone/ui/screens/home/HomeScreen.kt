package nl.hva.chatstone.ui.screens.home

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import nl.hva.chatstone.ui.composables.ChatstoneSnackbarHost
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
