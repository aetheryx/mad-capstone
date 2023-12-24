package nl.hva.capstone.ui.components

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import nl.hva.capstone.R
import nl.hva.capstone.api.model.output.User

@Composable
fun UserProfilePicture(user: User, modifier: Modifier) {
  val model = ImageRequest.Builder(LocalContext.current)
    .data(user.avatarURL)
    .fallback(R.drawable.default_pfp)
    .build()

  AsyncImage(
    model,
    contentDescription = "${user.username}'s profile picture",
    contentScale = ContentScale.Crop,
    modifier = modifier.clip(CircleShape)
  )
}
