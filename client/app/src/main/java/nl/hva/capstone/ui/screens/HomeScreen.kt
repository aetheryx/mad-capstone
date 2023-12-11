package nl.hva.capstone.ui.screens

import android.util.Log
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import nl.hva.capstone.viewmodels.SessionViewModel

@Composable
fun HomeScreen(sessionViewModel: SessionViewModel) {
  val me by sessionViewModel.me.observeAsState()

  Log.v("screen", "home")
  Text("$me")
}