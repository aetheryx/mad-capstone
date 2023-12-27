package nl.hva.capstone.ui.screens.home

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import nl.hva.capstone.R
import nl.hva.capstone.api.model.output.Conversation
import nl.hva.capstone.viewmodel.ConversationsViewModel
import nl.hva.capstone.viewmodel.SessionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavHostController, conversationsVM: ConversationsViewModel) {
  LaunchedEffect(Unit) {
    if (!conversationsVM.conversations.isInitialized) {
      conversationsVM.fetchConversations()
    }
  }

  Scaffold(
    topBar = {
      TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(),
        title = { Text(stringResource(R.string.conversations)) }
      )
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
    }
  ) {
    ConversationsList(navController, conversationsVM, Modifier.padding(top = it.calculateTopPadding()))
  }
}

