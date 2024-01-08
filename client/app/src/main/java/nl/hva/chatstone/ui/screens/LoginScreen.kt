package nl.hva.chatstone.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import nl.hva.chatstone.R
import nl.hva.chatstone.ui.composables.CredentialTextField
import nl.hva.chatstone.viewmodel.SessionState
import nl.hva.chatstone.viewmodel.SessionViewModel

@Composable
fun LoginScreen(
  navController: NavHostController,
  sessionVM: SessionViewModel,
  modifier: Modifier
) {
  val state by sessionVM.state.observeAsState()
  val error =
    if (state == SessionState.CREDENTIAL_ERROR) stringResource(R.string.password_is_incorrect)
    else null

  if (state == SessionState.READY) {
    navController.navigate("/conversations")
  }

  Column(
    modifier = modifier.padding(16.dp),
    verticalArrangement = Arrangement.SpaceBetween
  ) {
    Column(
      modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Icon(
        painterResource(R.drawable.hva),
        contentDescription = "Chatstone icon",
        modifier = Modifier.size(112.dp)
      )
    }

    Column(
      verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
    ) {
      CredentialFields(
        error,
        onSubmit = sessionVM::logIn
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

    Column {
      OutlinedButton(
        onClick = {
          navController.navigate("/signup")
        },
        colors = ButtonDefaults.outlinedButtonColors(),
        modifier = Modifier.fillMaxWidth()
      ) {
        Text(stringResource(R.string.create_new_account))
      }
    }
  }
}

@Composable
fun CredentialFields(
  error: String?,
  onSubmit: (String, String) -> Unit,
  buttonContent: @Composable RowScope.() -> Unit,
) {
  var username by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }

  CredentialTextField(
    value = username,
    onChanged = { username = it },
  )

  CredentialTextField(
    value = password,
    onChanged = { password = it },
    error = error,
    visualTransformation = PasswordVisualTransformation()
  )

  Button(
    onClick = { onSubmit(username, password) },
    modifier = Modifier.fillMaxWidth(),
    colors = ButtonDefaults.buttonColors(),
    content = buttonContent
  )
}