package nl.hva.capstone.ui.screens

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import coil.request.ImageRequest
import coil.compose.AsyncImage
import nl.hva.capstone.R
import nl.hva.capstone.viewmodels.SessionViewModel

@Composable
fun SignupScreen(
  navController: NavHostController,
  sessionViewModel: SessionViewModel
) {
  var imageUri: Uri? by remember { mutableStateOf(null) }

  val pickMedia = rememberLauncherForActivityResult(PickVisualMedia()) {
    imageUri = it
  }

  val model = ImageRequest.Builder(LocalContext.current)
    .data("https://firebasestorage.googleapis.com/v0/b/capstone-386f7.appspot.com/o/profile_pictures%2Ftest.png?alt=media&token=842b101f-4828-4388-b328-9eff16e9e265")
    .crossfade(true)
    .fallback(R.drawable.default_pfp)
    .build()

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp),
    verticalArrangement = Arrangement.SpaceBetween
  ) {
    Column(
      modifier = Modifier.clickable {
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
      LogInSection(
        error = null,
        onSubmit = { username, password ->
          val ref = sessionViewModel.storage.reference.child("profile_pictures/test.png")
          val task = ref.putFile(imageUri!!)

          task.addOnSuccessListener {
            Log.v("upload", "$it ${it.storage.downloadUrl.result}")
          }.addOnFailureListener {
            Log.v("upload", "$it")
          }
        }
      ) {
        Text(stringResource(R.string.create))
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