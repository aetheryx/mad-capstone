package nl.hva.capstone.ui.screens

import android.util.Log
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import nl.hva.capstone.R
import nl.hva.capstone.viewmodel.SessionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavHostController, sessionViewModel: SessionViewModel) {
  val conversationsViewModel = sessionViewModel.conversationsViewModel

  LaunchedEffect(Unit) {
    conversationsViewModel.fetchConversations()
  }

  val conversations by conversationsViewModel.conversations.observeAsState(emptyList())

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
          navController.navigate("/home/add-user")
        }
      ) {
        Icon(
          Icons.Filled.Add,
          contentDescription = "Add"
        )
      }
    }
  ) {
    Text("$conversations", modifier = Modifier.padding(it))
  }
}

