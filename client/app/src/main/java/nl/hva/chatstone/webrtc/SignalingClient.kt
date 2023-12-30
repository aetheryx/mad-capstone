package nl.hva.chatstone.webrtc

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import nl.hva.chatstone.ChatstoneApplication
import nl.hva.chatstone.api.ClientEvent
import nl.hva.chatstone.api.model.WebRTCPayload

class SignalingClient(
  private val application: ChatstoneApplication
) {
  private val sessionVM = application.sessionVM
  private val signalingScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

  private val sessionStateFlow = MutableStateFlow(WebRTCSessionState.Ready)
  val signalingCommandFlow = MutableSharedFlow<Pair<SignalingCommand, String>>()

  fun sendCommand(signalingCommand: SignalingCommand, message: String) {
    val payload = WebRTCPayload("$signalingCommand $message")
    val event = ClientEvent.WebRTCPayloadEvent(payload)
    sessionVM.websocket.sendMessage(event)
  }

  fun onWebRTCPayload(payload: String) {
    sessionStateFlow.value = WebRTCSessionState.Creating

    when {
      payload.startsWith(SignalingCommand.STATE.toString(), true) ->
        handleStateMessage(payload)

      payload.startsWith(SignalingCommand.OFFER.toString(), true) ->
        handleSignalingCommand(SignalingCommand.OFFER, payload)

      payload.startsWith(SignalingCommand.ANSWER.toString(), true) ->
        handleSignalingCommand(SignalingCommand.ANSWER, payload)

      payload.startsWith(SignalingCommand.ICE.toString(), true) ->
        handleSignalingCommand(SignalingCommand.ICE, payload)
    }
  }

  private fun handleStateMessage(message: String) {
    val state = getSeparatedMessage(message)
    sessionStateFlow.value = WebRTCSessionState.valueOf(state)
  }

  private fun handleSignalingCommand(command: SignalingCommand, text: String) {
    val value = getSeparatedMessage(text)
    signalingScope.launch {
      signalingCommandFlow.emit(command to value)
    }
  }

  private fun getSeparatedMessage(text: String) = text.substringAfter(' ')

  fun dispose() {
    sessionStateFlow.value = WebRTCSessionState.Offline
    signalingScope.cancel()
  }
}

enum class WebRTCSessionState {
  Active, // Offer and Answer messages has been sent
  Creating, // Creating session, offer has been sent
  Ready, // Both clients available and ready to initiate session
  Impossible, // We have less than two clients connected to the server
  Offline // unable to connect signaling server
}

enum class SignalingCommand {
  STATE, // Command for WebRTCSessionState
  OFFER, // to send or receive offer
  ANSWER, // to send or receive answer
  ICE // to send and receive ice candidates
}
