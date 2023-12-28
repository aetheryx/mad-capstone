package nl.hva.capstone.activities

import android.Manifest
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
import nl.hva.capstone.CapstoneApplication
import nl.hva.capstone.ui.theme.CapstoneTheme
import nl.hva.capstone.ui.windows.CapstoneAppWindow
import nl.hva.capstone.viewmodel.SessionState

private val permissions = arrayOf(
  Manifest.permission.CAMERA,
  Manifest.permission.RECORD_AUDIO,
  Manifest.permission.POST_NOTIFICATIONS,
)

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val sessionVM = (application as CapstoneApplication).sessionVM

    // enable edge-to-edge display
    enableEdgeToEdge()

    // request permissions
    requestPermissions(permissions, 0)

    // set up pre draw listener
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

    // set content
    setContent {
      CapstoneTheme {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = MaterialTheme.colorScheme.surface,
        ) {
          CapstoneAppWindow(sessionVM)
        }
      }
    }
  }
}