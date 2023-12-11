package nl.hva.capstone.ui.screens

import android.util.Log
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import kotlinx.coroutines.delay
import nl.hva.capstone.viewmodels.SessionState
import nl.hva.capstone.viewmodels.SessionViewModel

@Composable
fun HomeScreen(sessionViewModel: SessionViewModel) {
  val me by sessionViewModel.me.observeAsState()

  LaunchedEffect(Unit) {
    delay(750)
    sessionViewModel.state.postValue(SessionState.READY)
  }

  Log.v("screen", "home")
  Text("$me")

}