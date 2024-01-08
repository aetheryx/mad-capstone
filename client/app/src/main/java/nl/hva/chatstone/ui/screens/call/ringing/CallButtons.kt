package nl.hva.chatstone.ui.screens.call.ringing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Message
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import nl.hva.chatstone.viewmodel.CallState
import nl.hva.chatstone.viewmodel.CallViewModel
import nl.hva.chatstone.viewmodel.SessionViewModel

@Composable
fun CallButtons(
  sessionVM: SessionViewModel,
  ringing: Boolean
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceAround
  ) {
    if (ringing) {
      HangUpButtons(sessionVM.callVM)
    } else {
      DeclinedButtons(sessionVM)
    }
  }
}

@Composable
private fun HangUpButtons(callVM: CallViewModel) {
  CallButton(
    icon = Icons.Filled.CallEnd,
    containerColor = MaterialTheme.colorScheme.errorContainer,
    onClick = {
      callVM.hangUpCall()
    }
  )
}

@Composable
private fun DeclinedButtons(sessionVM: SessionViewModel) {
  val callVM = sessionVM.callVM

  CallButton(
    icon = Icons.Filled.Close,
    label = "Cancel",
    containerColor = MaterialTheme.colorScheme.secondaryContainer,
    onClick = {
      sessionVM.callVM.exitActivity()
    }
  )

  CallButton(
    icon = Icons.Filled.Message,
    label = "Message",
    containerColor = MaterialTheme.colorScheme.secondaryContainer,
    onClick = {
      sessionVM.callVM.exitActivity()
    }
  )

  CallButton(
    icon = Icons.Filled.Call,
    label = "Call again",
    containerColor = MaterialTheme.colorScheme.secondaryContainer,
    onClick = fun() {
      val state = callVM.callState.value
      val conversationID = when (state) {
        is CallState.Ringing -> state.conversationID
        else -> {
          sessionVM.callVM.exitActivity()
          return
        }
      }

      val conversation = sessionVM.conversationsVM.getConversation(conversationID)
      if (conversation != null) {
        callVM.call(conversation)
      } else {
        sessionVM.callVM.exitActivity()
      }
    }
  )
}
