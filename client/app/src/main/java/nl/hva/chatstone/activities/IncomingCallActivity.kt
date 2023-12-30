package nl.hva.chatstone.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import nl.hva.chatstone.ChatstoneApplication
import nl.hva.chatstone.ui.theme.ChatstoneTheme
import nl.hva.chatstone.ui.windows.IncomingCallWindow

class IncomingCallActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    val sessionVM = (application as ChatstoneApplication).sessionVM

    setContent {
      ChatstoneTheme {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = MaterialTheme.colorScheme.surface,
        ) {
          IncomingCallWindow(sessionVM)
        }
      }
    }
  }
}