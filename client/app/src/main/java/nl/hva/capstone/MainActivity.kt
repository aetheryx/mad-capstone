package nl.hva.capstone

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import nl.hva.capstone.service.NotificationService
import nl.hva.capstone.ui.theme.CapstoneTheme
import nl.hva.capstone.viewmodel.SessionState

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    requestPermissions(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE), 0)

    val intent = Intent(this, NotificationService::class.java)
    startService(intent)

//    val sessionManager: WebRtcSessionManager = WebRtcSessionManagerImpl(
//      context = this,
//      signalingClient = SignalingClient(),
//      peerConnectionFactory = StreamPeerConnectionFactory(this)
//    )

      val sessionVM = (application as CapstoneApplication).sessionVM
//    sessionVM.websocket.signalingClient = sessionManager.signalingClient
//    sessionManager.signalingClient.cws = sessionVM.websocket


    setContent {
      CapstoneTheme {
//        CompositionLocalProvider(LocalWebRtcSessionManager provides sessionManager) {
          Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surface,
          ) {
            CapstoneApp(sessionVM)
          }
//        }
      }
    }

    val content: View = findViewById(android.R.id.content)
    content.viewTreeObserver.addOnPreDrawListener(
      object : ViewTreeObserver.OnPreDrawListener {
        override fun onPreDraw(): Boolean {
          val ready = sessionVM.state.value != SessionState.INITIALISING

          println("ready: ${sessionVM.state.value} $ready")

          if (ready) {
            content.viewTreeObserver.removeOnPreDrawListener(this)
          }

          return ready
        }
      }
    )
  }
}