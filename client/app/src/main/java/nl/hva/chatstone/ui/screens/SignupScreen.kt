package nl.hva.chatstone.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import nl.hva.chatstone.R
import nl.hva.chatstone.ui.composables.CredentialTextField
import nl.hva.chatstone.util.resourceToURI
import nl.hva.chatstone.viewmodel.SessionState
import nl.hva.chatstone.viewmodel.SessionViewModel

private const val MIN_PASSWORD_LEN = 8
private const val MIN_USERNAME_LEN = 4

@Composable
fun SignupScreen(
  navController: NavHostController,
  sessionVM: SessionViewModel,
  modifier: Modifier
) {
  val context = LocalContext.current

  val username = remember { mutableStateOf("") }
  val password = remember { mutableStateOf("") }
  var usernameError by remember { mutableStateOf<String?>(null) }
  var passwordError by remember { mutableStateOf<String?>(null) }

  val imageUri = remember {
    mutableStateOf(resourceToURI(context, R.drawable.default_pfp))
  }

  val onClick = {
    usernameError = null
    passwordError = null

    if (password.value.length < MIN_PASSWORD_LEN) {
      passwordError = context.getString(
        R.string.min_chars,
        context.getString(R.string.password),
        MIN_PASSWORD_LEN
      )
    }
    if (username.value.length < MIN_USERNAME_LEN) {
      usernameError = context.getString(
        R.string.min_chars,
        context.getString(R.string.username),
        MIN_USERNAME_LEN
      )
    }

    if (usernameError == null && passwordError == null) {
      sessionVM.signUp(username.value, password.value, imageUri.value)
    }
  }

  val state by sessionVM.state.observeAsState()
  when (state) {
    SessionState.READY -> navController.navigate("/conversations")
    SessionState.CREDENTIAL_ERROR -> {
      usernameError = stringResource(R.string.username_is_taken)
    }
    else -> {}
  }

  Column(
    modifier = modifier.padding(16.dp),
    verticalArrangement = Arrangement.SpaceBetween
  ) {
    ImagePicker(imageUri)

    Column(
      verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
    ) {
      CredentialFields(
        username, password,
        usernameError, passwordError
      )

      Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(),
      ) {
        when (state) {
          SessionState.LOGGING_IN -> CircularProgressIndicator(
            color = MaterialTheme.colorScheme.onPrimary,
            strokeWidth = 3.dp,
            modifier = Modifier.size(24.dp),
          )
          else -> Text(stringResource(R.string.create))
        }
      }
    }

    Column {
      OutlinedButton(
        onClick = {
          navController.navigate("/login")
        },
        colors = ButtonDefaults.outlinedButtonColors(),
        modifier = Modifier.fillMaxWidth()
      ) {
        Text(stringResource(R.string.log_in_with_existing_account))
      }
    }
  }
}

@Composable
private fun ImagePicker(
  imageUri: MutableState<Uri>
) {
  val model = ImageRequest.Builder(LocalContext.current)
    .data(imageUri.value)
    .crossfade(true)
    .fallback(R.drawable.default_pfp)
    .build()

  val pickMedia = rememberLauncherForActivityResult(
    contract = PickVisualMedia()
  ) { uri ->
    if (uri != null) {
      imageUri.value = uri
    }
  }

  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier
      .fillMaxWidth()
      .clickable {
        val request = PickVisualMediaRequest(PickVisualMedia.ImageOnly)
        pickMedia.launch(request)
      }
  ) {
    AsyncImage(
      model,
      contentDescription = "Profile picture",
      contentScale = ContentScale.Crop,
      modifier = Modifier
        .size(160.dp)
        .clip(CircleShape)
    )
  }
}

@Composable
private fun CredentialFields(
  username: MutableState<String>,
  password: MutableState<String>,
  usernameError: String?,
  passwordError: String?,
) {
  CredentialTextField(
    label = R.string.username,
    value = username.value,
    onChanged = { username.value = it },
    error = usernameError
  )

  CredentialTextField(
    label = R.string.password,
    value = password.value,
    onChanged = { password.value = it },
    error = passwordError,
    visualTransformation = PasswordVisualTransformation()
  )
}
