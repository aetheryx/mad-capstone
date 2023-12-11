package nl.hva.capstone

import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import nl.hva.capstone.ui.theme.CapstoneTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContent {
      CapstoneTheme {
        // A surface container using the 'background' color from the theme
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = MaterialTheme.colorScheme.surface
        ) {
          CapstoneApp()
        }
      }
    }

    val ready = false

//    val content: View = findViewById(android.R.id.content)
//    content.viewTreeObserver.addOnPreDrawListener(
//      object : ViewTreeObserver.OnPreDrawListener {
//        override fun onPreDraw(): Boolean {
//          // Check whether the initial data is ready.
//          return if (ready) {
//            // The content is ready. Start drawing.
//            content.viewTreeObserver.removeOnPreDrawListener(this)
//            true
//          } else {
//            // The content isn't ready. Suspend.
//            false
//          }
//        }
//      }
//    )
  }
}