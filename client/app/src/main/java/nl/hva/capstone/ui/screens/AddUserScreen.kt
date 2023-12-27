package nl.hva.capstone.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import nl.hva.capstone.R
import nl.hva.capstone.viewmodel.ConversationCreateState
import nl.hva.capstone.viewmodel.ConversationsViewModel
import nl.hva.capstone.viewmodel.SessionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddUserScreen(navController: NavHostController, conversationsVM: ConversationsViewModel) {
  Scaffold(
    topBar = {
      TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(),
        title = { Text("Add by Username") },
        navigationIcon = {
          IconButton(
            onClick = navController::popBackStack
          ) {
            Icon(
              Icons.Default.ArrowBack,
              contentDescription = "Go back"
            )
          }
        },
      )
    }
  ) {
    Column(modifier = Modifier.padding(it)) {
      AddUserView(navController, conversationsVM)
    }
  }
}

@Composable
private fun AddUserView(navController: NavHostController, conversationsVM: ConversationsViewModel) {
  var username by remember { mutableStateOf("") }
  val createState by conversationsVM.createState.observeAsState()
  val isError = createState is ConversationCreateState.Errored

  if (createState is ConversationCreateState.Created) {
    navController.navigate("/conversations/${createState!!.id}")
  }

  TextField(
    value = username,
    onValueChange = { username = it },
    label = { Text(stringResource(R.string.username)) },
    modifier = Modifier.fillMaxWidth(),
    colors = TextFieldDefaults.colors(),
    shape = TextFieldDefaults.shape,
    isError = isError,
    supportingText = {
      if (isError) Text(
        stringResource(R.string.user_not_found),
        color = MaterialTheme.colorScheme.error,
      )
    }
  )

  Button(
    colors = ButtonDefaults.buttonColors(),
    onClick = {
      conversationsVM.createConversation(username)
    }
  ) {
    when (createState) {
      is ConversationCreateState.Creating -> {
        CircularProgressIndicator(
          color = MaterialTheme.colorScheme.onPrimary,
          strokeWidth = 3.dp,
          modifier = Modifier.size(24.dp),
        )
      }

      else -> {
        Text(stringResource(R.string.start_conversation))
      }
    }
  }
}

