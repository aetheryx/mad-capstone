package nl.hva.chatstone.viewmodel

import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import nl.hva.chatstone.ChatstoneApplication
import nl.hva.chatstone.activities.OngoingCallActivity
import nl.hva.chatstone.api.ClientEvent
import nl.hva.chatstone.api.model.CallResponse
import nl.hva.chatstone.api.model.WebRTCPayload
import nl.hva.chatstone.api.model.input.IncomingCallOffer
import nl.hva.chatstone.api.model.output.Conversation
import nl.hva.chatstone.api.model.output.OutgoingCallOffer
import java.time.Instant

class CallViewModel(
  val application: ChatstoneApplication,
) : AndroidViewModel(application) {
  private val scope = CoroutineScope(Dispatchers.IO)
  private val sessionVM = application.sessionVM
  private val conversationsVM = sessionVM.conversationsVM

  val callState = MutableLiveData<CallState>(CallState.None)

  fun call(conversation: Conversation) {
    val callID = Instant.now().epochSecond.toInt()
    val offer = IncomingCallOffer(
      calleeID = conversation.otherParticipant.id,
      conversationID = conversation.id,
      callID = callID
    )
    val event = ClientEvent.CallOfferEvent(offer)
    sessionVM.websocket.sendMessage(event)

    callState.value = CallState.Ringing.Outgoing(
      conversationID = conversation.id,
      callID = callID
    )
  }

  fun hangUpCall() {
    val callID = when (val callState = callState.value) {
      is CallState.Connected -> callState.callID
      is CallState.Ringing.Outgoing -> callState.callID
      else -> return
    }

    val event = ClientEvent.CallHangUpEvent(callID)
    sessionVM.websocket.sendMessage(event)
    exitActivity()
  }

  fun launchActivity() {
    val intent = Intent(application, OngoingCallActivity::class.java)
    application.mainActivity!!.startActivity(intent)
  }

  fun exitActivity() {
    application.callActivity?.finish()
    callState.postValue(CallState.None)
  }

  fun onCallOffer(callOffer: OutgoingCallOffer) {
    val state = CallState.Ringing.Incoming(
      conversationID = callOffer.conversationID,
      callID = callOffer.callID
    )

    callState.postValue(state)
  }

  fun onCallHangUp() {
    exitActivity()
    application.webRtcSessionManager.disconnect()
  }

  fun onCallResponse(data: CallResponse) = scope.launch {
    delay(1000)

    if (data.accepted) {
      callState.postValue(CallState.Connected(data.callID))
    } else {
      val state = callState.value as CallState.Ringing.Outgoing
      val newState = CallState.Ringing.Declined(
        conversationID = state.conversationID,
        callID = state.callID
      )

      callState.postValue(newState)
    }
  }

  fun acceptCall() {
    val state = callState.value
    val callID = when (state) {
      is CallState.Ringing.Incoming -> state.callID
      else -> return
    }

    sendCallResponse(true, CallState.Connected(callID))
  }

  fun declineCall() {
    sendCallResponse(false, CallState.None)
  }

  private fun sendCallResponse(accepted: Boolean, newCallState: CallState) {
    val state = callState.value
    if (state !is CallState.Ringing.Incoming) return

    val conversation = conversationsVM.getConversation(state.conversationID) ?: return
    val callerID = conversation.otherParticipant.id

    val response = CallResponse(
      callerID = callerID,
      accepted = accepted,
      callID = state.callID
    )
    val event = ClientEvent.CallResponseEvent(response)
    sessionVM.websocket.sendMessage(event)

    callState.value = newCallState
  }

  fun onWebRTCPayload(data: WebRTCPayload) {
    application.signalingClient.onWebRTCPayload(data.payload)
  }
}