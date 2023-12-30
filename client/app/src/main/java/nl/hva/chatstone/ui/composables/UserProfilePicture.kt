package nl.hva.chatstone.ui.composables

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import nl.hva.chatstone.api.model.output.User

@Composable
fun UserProfilePicture(user: User, modifier: Modifier) {
  val model = user.avatarModel(LocalContext.current)

  AsyncImage(
    model,
    contentDescription = "${user.username}'s profile picture",
    contentScale = ContentScale.Crop,
    modifier = modifier.clip(CircleShape)
  )
}
