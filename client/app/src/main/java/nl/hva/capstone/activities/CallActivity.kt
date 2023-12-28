package nl.hva.capstone.activities

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
import nl.hva.capstone.ui.windows.CallWindow

class CallActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    val sessionVM = (application as CapstoneApplication).sessionVM

    setContent {
      CapstoneTheme {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = MaterialTheme.colorScheme.surface,
        ) {
          CallWindow(sessionVM)
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
