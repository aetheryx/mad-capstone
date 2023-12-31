package nl.hva.chatstone.ui.windows

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import nl.hva.chatstone.ui.screens.call.ringing.RingingCallScreen
import nl.hva.chatstone.ui.screens.callscreen.VideoCallScreen
import nl.hva.chatstone.viewmodel.CallState
import nl.hva.chatstone.viewmodel.SessionViewModel

@Composable
fun OngoingCallWindow(sessionVM: SessionViewModel) {
  Column(Modifier.fillMaxSize()) {
    OngoingCallWindowContent(sessionVM)
  }
}

@Composable
private fun OngoingCallWindowContent(sessionVM: SessionViewModel) {
  val callState by sessionVM.callVM.callState.observeAsState()

  when (callState) {
    is CallState.Ringing ->
      RingingCallScreen(sessionVM, callState as CallState.Ringing)

    is CallState.Connected ->
      VideoCallScreen(sessionVM)

    is CallState.None, null ->
      Unit
  }
}