package nl.hva.chatstone.ui.composables

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import nl.hva.chatstone.R
import nl.hva.chatstone.viewmodel.ConversationsViewModel

@Composable
fun ChatstoneSnackbarHost(conversationsVM: ConversationsViewModel) {
  val scope = rememberCoroutineScope()
  val snackbarHostState = remember { SnackbarHostState() }
  val context = LocalContext.current

  val reconnecting by conversationsVM.sessionVM.websocket.reconnecting.observeAsState()

  LaunchedEffect(reconnecting) {
    scope.launch {
      if (reconnecting == true) {
        snackbarHostState.showSnackbar(
          message = context.getString(R.string.no_connection),
          duration = SnackbarDuration.Indefinite,
        )
      } else {
        snackbarHostState.currentSnackbarData?.dismiss()
      }
    }
  }

  SnackbarHost(snackbarHostState)
}