package nl.hva.capstone.activities

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
import nl.hva.capstone.CapstoneApplication
import nl.hva.capstone.ui.theme.CapstoneTheme
import nl.hva.capstone.ui.windows.OngoingCallWindow
import nl.hva.capstone.webrtc.SignalingClient
import nl.hva.capstone.webrtc.peer.StreamPeerConnectionFactory
import nl.hva.capstone.webrtc.sessions.LocalWebRtcSessionManager
import nl.hva.capstone.webrtc.sessions.WebRtcSessionManagerImpl

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

    val sessionManager = WebRtcSessionManagerImpl(
      context = this,
      signalingClient = (application as CapstoneApplication).signalingClient,
      StreamPeerConnectionFactory(this)
    )

    intent.extras?.getInt("notification_id")?.let {
      val manager = application.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
      manager.cancel(it)
    }

    val sessionVM = (application as CapstoneApplication).sessionVM
    Log.v("OngoingCallActivity", "accepting")
    sessionVM.callVM.acceptCall()

    setContent {
      CapstoneTheme {
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

