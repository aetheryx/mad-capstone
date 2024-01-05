package nl.hva.chatstone.activities

import android.Manifest
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import nl.hva.chatstone.ui.theme.ChatstoneTheme
import nl.hva.chatstone.ui.theme.surfaceContainer
import nl.hva.chatstone.ui.windows.OngoingCallWindow

private val permissions = arrayOf(
  Manifest.permission.CAMERA,
  Manifest.permission.RECORD_AUDIO,
  Manifest.permission.POST_NOTIFICATIONS,
)

class OngoingCallActivity : CallActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    enableEdgeToEdge()
    super.onCreate(savedInstanceState)

    // request permissions
    requestPermissions(permissions, 0)

    val sessionVM = application.sessionVM
    Log.v("OngoingCallActivity", "accepting ${application.webRtcSessionManager}")
    sessionVM.callVM.acceptCall()

    setContent {
      ChatstoneTheme(
        statusBarColor = { it.surface }
      ) {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = MaterialTheme.colorScheme.surface,
        ) {
          OngoingCallWindow(sessionVM)
        }
      }
    }
  }
}

