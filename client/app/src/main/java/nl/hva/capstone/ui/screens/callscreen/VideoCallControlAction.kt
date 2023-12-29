package nl.hva.capstone.ui.screens.callscreen

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.core.graphics.toColor
import nl.hva.capstone.R

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
  val icon: Painter,
  val iconTint: Color,
  val background: Color,
  val callAction: CallAction
)

@Composable
fun buildDefaultCallControlActions(
  callMediaState: CallMediaState
): List<VideoCallControlAction> {
  val microphoneIcon =
    painterResource(
      id = if (callMediaState.isMicrophoneEnabled) {
        R.drawable.default_pfp
      } else {
        R.drawable.default_pfp
      }
    )

  val cameraIcon = painterResource(
    id = if (callMediaState.isCameraEnabled) {
      R.drawable.default_pfp
    } else {
      R.drawable.default_pfp
    }
  )

  return listOf(
    VideoCallControlAction(
      icon = microphoneIcon,
      iconTint = Color.White,
      background = Color(R.color.black),
      callAction = CallAction.ToggleMicroPhone(callMediaState.isMicrophoneEnabled)
    ),
    VideoCallControlAction(
      icon = cameraIcon,
      iconTint = Color.White,
      background = Color(R.color.black),
      callAction = CallAction.ToggleCamera(callMediaState.isCameraEnabled)
    ),
    VideoCallControlAction(
      icon = painterResource(id = R.drawable.default_pfp),
      iconTint = Color.White,
      background = Color(R.color.black),
      callAction = CallAction.FlipCamera
    ),
    VideoCallControlAction(
      icon = painterResource(id = R.drawable.default_pfp),
      iconTint = Color.White,
      background = Color(R.color.black),
      callAction = CallAction.LeaveCall
    )
  )
}
