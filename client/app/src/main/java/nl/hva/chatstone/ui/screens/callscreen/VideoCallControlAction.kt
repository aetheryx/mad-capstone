package nl.hva.chatstone.ui.screens.callscreen

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.FlipCameraIos
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import nl.hva.chatstone.R

sealed class CallAction {
  data class ToggleMicroPhone(
    val isEnabled: Boolean
  ) : CallAction()

  data class ToggleCamera(
    val isEnabled: Boolean
  ) : CallAction()

  object FlipCamera : CallAction()

  object LeaveCall : CallAction()
}

data class VideoCallControlAction(
  val icon: ImageVector,
  val iconTint: Color,
  val background: Color,
  val callAction: CallAction
)

@Composable
fun buildDefaultCallControlActions(
  callMediaState: CallMediaState
): List<VideoCallControlAction> {
  val microphoneIcon = Icons.Filled.let {
    if (callMediaState.isMicrophoneEnabled) it.Mic else it.MicOff
  }

  return listOf(
    VideoCallControlAction(
      icon = microphoneIcon,
      iconTint = Color.White,
      background = Color(R.color.black),
      callAction = CallAction.ToggleMicroPhone(callMediaState.isMicrophoneEnabled)
    ),
    VideoCallControlAction(
      icon = Icons.Filled.FlipCameraIos,
      iconTint = Color.White,
      background = Color(R.color.black),
      callAction = CallAction.FlipCamera
    ),
    VideoCallControlAction(
      icon = Icons.Filled.CallEnd,
      iconTint = Color.White,
      background = Color(R.color.black),
      callAction = CallAction.LeaveCall
    )
  )
}
