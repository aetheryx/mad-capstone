package nl.hva.chatstone.ui.screens.call.ringing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import nl.hva.chatstone.R
import nl.hva.chatstone.api.model.output.User
import nl.hva.chatstone.ui.composables.UserProfilePicture
import nl.hva.chatstone.viewmodel.CallState
import nl.hva.chatstone.viewmodel.SessionViewModel

@Composable
fun RingingCallScreen(
  sessionVM: SessionViewModel,
  callState: CallState.Ringing,
) {
  val conversationsVM = sessionVM.conversationsVM
  val conversation = conversationsVM.getConversation(callState.conversationID) ?: return
  val ringing = when (callState) {
    is CallState.Ringing.Outgoing -> true
    is CallState.Ringing.Declined -> false
    else -> return
  }

  Column(
    modifier = Modifier
      .safeDrawingPadding()
      .padding(16.dp, 40.dp)
      .fillMaxSize(),
    verticalArrangement = Arrangement.SpaceBetween
  ) {
    CallParticipant(conversation.otherParticipant, ringing)
    CallButtons(sessionVM, ringing)
  }
}

@Composable
private fun CallParticipant(
  user: User,
  ringing: Boolean
) {
  Column(
    modifier = Modifier.fillMaxWidth(),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    UserProfilePicture(user, Modifier.size(192.dp))

    Text(
      user.username,
      style = MaterialTheme.typography.displayMedium
    )

    Text(
      stringResource(if (ringing) R.string.ringing else R.string.call_declined),
      style = MaterialTheme.typography.titleLarge
    )
  }
}

