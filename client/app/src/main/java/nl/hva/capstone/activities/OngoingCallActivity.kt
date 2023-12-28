package nl.hva.capstone.activities

import android.app.NotificationManager
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import nl.hva.capstone.CapstoneApplication
import nl.hva.capstone.ui.theme.CapstoneTheme
import nl.hva.capstone.ui.windows.OngoingCallWindow

class OngoingCallActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    intent.extras?.getInt("notification_id")?.let {
      val manager = application.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
      manager.cancel(it)
    }

    val sessionVM = (application as CapstoneApplication).sessionVM

    setContent {
      CapstoneTheme {
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

//    val sessionManager: WebRtcSessionManager = WebRtcSessionManagerImpl(
//      context = this,
//      signalingClient = SignalingClient(),
//      peerConnectionFactory = StreamPeerConnectionFactory(this)
//    )
//    sessionVM.websocket.signalingClient = sessionManager.signalingClient
//    sessionManager.signalingClient.cws = sessionVM.websocket
//        CompositionLocalProvider(LocalWebRtcSessionManager provides sessionManager) {
