package nl.hva.capstone.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import nl.hva.capstone.R
import nl.hva.capstone.viewmodels.SessionState
import nl.hva.capstone.viewmodels.SessionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
  navController: NavHostController,
  sessionViewModel: SessionViewModel
) {
  var username by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }

  val state by sessionViewModel.state.observeAsState()
  val isError = state == SessionState.CREDENTIAL_ERROR

  if (state == SessionState.READY) {
    navController.navigate("/home")
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
  ) {
    TextField(
      value = username,
      onValueChange = { username = it },
      label = { Text(stringResource(R.string.username)) },
      modifier = Modifier.fillMaxWidth(),
      colors = TextFieldDefaults.textFieldColors(),
      shape = TextFieldDefaults.filledShape,
    )

    TextField(
      value = password,
      onValueChange = { password = it },
      label = { Text(stringResource(R.string.password)) },
      modifier = Modifier.fillMaxWidth(),
      colors = TextFieldDefaults.textFieldColors(),
      shape = TextFieldDefaults.filledShape,
      isError = isError,
      visualTransformation = PasswordVisualTransformation(),
      supportingText = {
        if (isError) {
          Text(
            stringResource(R.string.password_is_incorrect),
            color = MaterialTheme.colorScheme.error,
          )
        }
      },
      trailingIcon = {
        if (isError) {
          Icon(
            Icons.Filled.Error,
            contentDescription = "Error",
            tint = MaterialTheme.colorScheme.error
          )
        }
      }
    )

    Button(
      onClick = {
        sessionViewModel.logIn(username, password)
      },
      modifier = Modifier.fillMaxWidth(),
      colors = ButtonDefaults.buttonColors()
    ) {
      when (state) {
        SessionState.LOGGING_IN -> {
          CircularProgressIndicator(
            color = MaterialTheme.colorScheme.onPrimary,
            strokeWidth = 3.dp,
            modifier = Modifier.size(24.dp),
          )
        }
        else -> {
          Text(stringResource(R.string.log_in))
        }
      }
    }
  }
}
