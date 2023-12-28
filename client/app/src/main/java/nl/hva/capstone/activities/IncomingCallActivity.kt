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
import nl.hva.capstone.ui.windows.IncomingCallWindow

class IncomingCallActivity : ComponentActivity() {
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
          IncomingCallWindow(sessionVM)
        }
      }
    }
  }
}