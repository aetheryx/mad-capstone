package nl.hva.chatstone.ui.screens.callscreen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import nl.hva.chatstone.webrtc.LocalWebRtcSessionManager

@Composable
fun VideoCallScreen(
  sessionVM: SessionViewModel
) {
  val callVM = sessionVM.callVM
  val sessionManager = LocalWebRtcSessionManager.current

  LaunchedEffect(key1 = Unit) {
    sessionManager.onSessionScreenReady()
  }

  Box(
    modifier = Modifier.fillMaxSize()
  ) {
    var parentSize: IntSize by remember { mutableStateOf(IntSize(0, 0)) }

    val remoteVideoTrackState by sessionManager.remoteVideoTrackFlow.collectAsState(null)
    val remoteVideoTrack = remoteVideoTrackState

    val localVideoTrackState by sessionManager.localVideoTrackFlow.collectAsState(null)
    val localVideoTrack = localVideoTrackState

    var callMediaState by remember { mutableStateOf(CallMediaState()) }

    if (remoteVideoTrack != null) {
      VideoRenderer(
        videoTrack = remoteVideoTrack,
        modifier = Modifier
          .fillMaxSize()
          .onSizeChanged { parentSize = it }
      )
    }

    if (localVideoTrack != null && callMediaState.isCameraEnabled) {
      FloatingVideoRenderer(
        modifier = Modifier
          .size(width = 150.dp, height = 210.dp)
          .clip(RoundedCornerShape(16.dp))
          .align(Alignment.TopEnd),
        videoTrack = localVideoTrack,
        parentBounds = parentSize,
        paddingValues = PaddingValues(0.dp)
      )
    }

    VideoCallControls(
      modifier = Modifier
        .fillMaxWidth()
        .align(Alignment.BottomCenter),
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
            sessionManager.disconnect()
            callVM.hangUpCall()
            callVM.exitActivity()
          }
        }
      }
    )
  }
}
