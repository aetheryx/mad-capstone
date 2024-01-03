package nl.hva.chatstone.ui.screens.call.ringing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nl.hva.chatstone.R
import nl.hva.chatstone.api.model.output.User
import nl.hva.chatstone.ui.composables.UserProfilePicture
import nl.hva.chatstone.viewmodel.CallState
import nl.hva.chatstone.viewmodel.CallViewModel
import nl.hva.chatstone.viewmodel.SessionViewModel

@Composable
fun OutgoingRingingCallScreen(
  sessionVM: SessionViewModel,
  callState: CallState.Ringing.Outgoing,
) {
  val conversationsVM = sessionVM.conversationsVM
  val conversation = conversationsVM.getConversation(callState.conversationID) ?: return

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp),
    verticalArrangement = Arrangement.SpaceBetween
  ) {
    CallParticipant(conversation.otherParticipant, callState)
    CallButtons(sessionVM.callVM)
  }
}

@Composable
private fun CallParticipant(
  user: User,
  callState: CallState.Ringing.Outgoing
) {
  Column(
    modifier = Modifier.fillMaxWidth(),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    UserProfilePicture(user, Modifier.size(160.dp))

    Text(
      user.username,
      style = MaterialTheme.typography.headlineLarge
    )

    Text(
      stringResource(if (callState.declined) R.string.call_declined else R.string.ringing),
      style = MaterialTheme.typography.titleLarge.copy(fontSize = 18.sp)
    )
  }
}

@Composable
private fun CallButtons(callVM: CallViewModel) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.Center
  ) {
    HangUpButton(callVM)
  }
}

@Composable
private fun HangUpButton(callVM: CallViewModel) {
  IconButton(
    modifier = Modifier.size(64.dp),
    colors = IconButtonDefaults.filledIconButtonColors(
      containerColor = MaterialTheme.colorScheme.errorContainer
    ),
    onClick = {
      callVM.hangUpCall()
    },
  ) {
    Icon(
      Icons.Filled.CallEnd,
      contentDescription = "Cancel",
      modifier = Modifier.size(40.dp)
    )
  }
}
