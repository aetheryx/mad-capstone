package nl.hva.capstone.ui.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import nl.hva.capstone.viewmodels.SessionViewModel

@Composable
fun HomeScreen(sessionViewModel: SessionViewModel) {
  val me by sessionViewModel.me.observeAsState()

  LaunchedEffect(Unit) {
    sessionViewModel.getMe()
  }

  Text("$me")
}