package nl.hva.capstone.ui.windows

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import nl.hva.capstone.viewmodel.CallState
import nl.hva.capstone.viewmodel.SessionViewModel

@Composable
fun CallWindow(sessionVM: SessionViewModel) {
  val conversationsVM = sessionVM.conversationsVM
  val callState by conversationsVM.callState.observeAsState(CallState.None)

//  when (callState) {
//    is
//  }
}