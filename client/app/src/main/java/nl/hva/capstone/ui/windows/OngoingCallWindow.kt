package nl.hva.capstone.ui.windows

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import nl.hva.capstone.viewmodel.SessionViewModel

@Composable
fun OngoingCallWindow(sessionVM: SessionViewModel) {
  Scaffold(
    modifier = Modifier.safeDrawingPadding().fillMaxSize()
  ) {
    val modifier = Modifier.fillMaxSize().padding(it)
    Column(modifier) {
      Text("ongoing call window")
    }
  }
}