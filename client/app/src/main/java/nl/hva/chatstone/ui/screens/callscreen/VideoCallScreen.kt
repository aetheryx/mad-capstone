package nl.hva.chatstone.ui.screens.callscreen

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import nl.hva.chatstone.viewmodel.SessionViewModel

@Composable
fun VideoCallScreen(
  sessionVM: SessionViewModel
) {
  val callVM = sessionVM.callVM
  val sessionManager = sessionVM.application.webRtcSessionManager

  DisposableEffect(Unit) {
    Log.v("VideoCallScreen", "onready")
    sessionManager.onSessionScreenReady()

    onDispose {
      Log.v("VideoCallScreen", "ondispose")
      sessionManager.disconnect()
    }
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .statusBarsPadding()
  ) {
    var parentSize: IntSize by remember { mutableStateOf(IntSize(0, 0)) }

    val remoteVideoTrack by sessionManager.remoteVideoTrackData.observeAsState()
    val localVideoTrack by sessionManager.localVideoTrackData.observeAsState()

    Log.v("VideoCallScreen", "$remoteVideoTrack $localVideoTrack")

    var callMediaState by remember { mutableStateOf(CallMediaState()) }

    if (remoteVideoTrack != null) {
      VideoRenderer(
        sessionManager,
        videoTrack = remoteVideoTrack!!,
        modifier = Modifier
          .fillMaxSize()
          .onSizeChanged { parentSize = it }
      )
    }

    Log.v("VideoCallScreen", "$localVideoTrack ${callMediaState.isCameraEnabled}")
    if (localVideoTrack != null && callMediaState.isCameraEnabled) {
      FloatingVideoRenderer(
        sessionManager,
        modifier = Modifier
          .size(width = 150.dp, height = 210.dp)
          .clip(RoundedCornerShape(16.dp))
          .align(Alignment.TopEnd),
        videoTrack = localVideoTrack!!,
        parentBounds = parentSize,
        paddingValues = PaddingValues(0.dp)
      )
    }

    VideoCallControls(
      modifier = Modifier
        .fillMaxWidth()
        .align(Alignment.BottomCenter)
        .navigationBarsPadding(),
      callMediaState = callMediaState,
      onCallAction = {
        when (it) {
          is CallAction.ToggleMicroPhone -> {
            val enabled = callMediaState.isMicrophoneEnabled.not()
            callMediaState = callMediaState.copy(isMicrophoneEnabled = enabled)
            sessionManager.enableMicrophone(enabled)
          }

          is CallAction.ToggleCamera -> {
            val enabled = callMediaState.isCameraEnabled.not()
            callMediaState = callMediaState.copy(isCameraEnabled = enabled)
            sessionManager.enableCamera(enabled)
          }

          CallAction.FlipCamera -> sessionManager.flipCamera()
          CallAction.LeaveCall -> {
            callVM.hangUpCall()
          }
        }
      }
    )
  }
}
