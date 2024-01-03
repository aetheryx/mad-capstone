package nl.hva.chatstone.activities

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import nl.hva.chatstone.ChatstoneApplication
import nl.hva.chatstone.ui.theme.ChatstoneTheme
import nl.hva.chatstone.ui.windows.OngoingCallWindow
import nl.hva.chatstone.webrtc.peer.StreamPeerConnectionFactory
import nl.hva.chatstone.webrtc.LocalWebRtcSessionManager
import nl.hva.chatstone.webrtc.WebRtcSessionManager

private val permissions = arrayOf(
  Manifest.permission.CAMERA,
  Manifest.permission.RECORD_AUDIO,
  Manifest.permission.POST_NOTIFICATIONS,
)

class OngoingCallActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    // request permissions
    requestPermissions(permissions, 0)

    val sessionManager = WebRtcSessionManager(
      context = this,
      signalingClient = (application as ChatstoneApplication).signalingClient,
      StreamPeerConnectionFactory(this)
    )

    intent.extras?.getInt("notification_id")?.let {
      val manager = application.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
      manager.cancel(it)
    }

    val sessionVM = (application as ChatstoneApplication).sessionVM
    Log.v("OngoingCallActivity", "accepting")
    sessionVM.callVM.acceptCall()

    setContent {
      ChatstoneTheme {
        CompositionLocalProvider(LocalWebRtcSessionManager provides sessionManager) {
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
}

