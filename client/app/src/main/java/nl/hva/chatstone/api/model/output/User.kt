package nl.hva.chatstone.api.model.output

import android.app.Person
import android.content.Context
import android.graphics.drawable.Icon
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.drawable.toBitmap
import coil.imageLoader
import coil.request.ImageRequest
import coil.transform.CircleCropTransformation
import kotlinx.serialization.Serializable
import nl.hva.chatstone.R
import nl.hva.chatstone.api.ChatstoneApi

@Serializable
data class User (
  val id: Int,
  val username: String,
  val password: String,
  val avatar: String
) {
  val avatarURL get() = "${ChatstoneApi.BASE_URL}/cdn/proxy/${avatar}"

  fun avatarModel(context: Context) = ImageRequest.Builder(context)
    .data(avatarURL)
    .transformations(CircleCropTransformation())
    .fallback(R.drawable.default_pfp)
    .build()

  suspend fun getIcon(context: Context): Icon {
    val request = avatarModel(context)
    val result = context.imageLoader.execute(request)
    val bitmap = result.drawable?.toBitmap()
    return Icon.createWithBitmap(bitmap)
  }

  suspend fun toPerson(context: Context): Person {
    val icon = getIcon(context)

    return Person.Builder()
      .setName(username)
      .setIcon(icon)
      .build()
  }
}
