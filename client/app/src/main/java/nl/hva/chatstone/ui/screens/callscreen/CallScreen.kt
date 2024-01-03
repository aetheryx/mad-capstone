package nl.hva.chatstone.ui.screens.callscreen

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import nl.hva.chatstone.api.ClientEvent
import nl.hva.chatstone.api.model.CallResponse
import nl.hva.chatstone.viewmodel.CallState
import nl.hva.chatstone.viewmodel.ConversationsViewModel

@Composable
fun CallScreen(
  navController: NavHostController,
  conversationID: Int,
  conversationsVM: ConversationsViewModel
) {
//  val callState by conversationsVM.callState.observeAsState()

//  when (callState) {
//    is CallState.Connected -> ConnectedCallScreen()
//    is CallState.Ringing.Incoming -> IncomingCallScreen(callState as CallState.Ringing.Incoming, navController, conversationsVM)
//    is CallState.Ringing.Outgoing -> OutgoingCallScreen(navController, conversationsVM)
//    else -> {}
//  }
}

@Composable
private fun ConnectedCallScreen() {
  Text("connected")
}

@Composable
private fun OutgoingCallScreen(
  navController: NavHostController,
  conversationsVM: ConversationsViewModel
) {
  Text("Outgoing")
}

@Composable
private fun IncomingCallScreen(
  callState: CallState.Ringing.Incoming,
  navController: NavHostController,
  conversationsVM: ConversationsViewModel
) {
  val onClick = { accepted: Boolean ->
//    val response = CallResponse(callState.conversationID, accepted)
//    val event = ClientEvent.CallResponseEvent(response)
//    conversationsVM.sessionVM.websocket.sendMessage(event)

//    conversationsVM.callState.postValue(CallState.Connected())
  }

  Row() {
    Button(onClick = { onClick(true) }) {
      Text("Accept")
    }

    Button(onClick = { onClick(false) }) {
      Text("Decline")
    }
  }
}
