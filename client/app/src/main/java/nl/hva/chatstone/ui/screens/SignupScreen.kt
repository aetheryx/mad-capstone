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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import nl.hva.chatstone.R
import nl.hva.chatstone.viewmodel.SessionState
import nl.hva.chatstone.viewmodel.SessionViewModel

@Composable
fun SignupScreen(
  navController: NavHostController,
  sessionVM: SessionViewModel,
  modifier: Modifier
) {
  var imageUri: Uri? by remember { mutableStateOf(null) }
  val state by sessionVM.state.observeAsState()

  if (state == SessionState.READY) {
    navController.navigate("/conversations")
  }

  val pickMedia = rememberLauncherForActivityResult(PickVisualMedia()) {
    imageUri = it
  }

  val model = ImageRequest.Builder(LocalContext.current)
    .data(imageUri)
    .crossfade(true)
    .fallback(R.drawable.default_pfp)
    .build()

  Column(
    modifier = modifier.padding(16.dp),
    verticalArrangement = Arrangement.SpaceBetween
  ) {
    // TODO: fix layout
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier
        .fillMaxWidth()
        .clickable {
          pickMedia.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
        }
    ) {
      AsyncImage(
        model,
        contentDescription = "Profile picture",
        contentScale = ContentScale.Crop,
        modifier = Modifier
          .size(200.dp)
          .clip(CircleShape)
      )
    }

    Column(
      verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
    ) {
      // TODO: error handling
      LogInSection(
        error = null,
        onSubmit = { username, password ->
          sessionVM.signUp(username, password, imageUri!!)
        }
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