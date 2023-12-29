package nl.hva.capstone.viewmodel

import android.content.Intent
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import nl.hva.capstone.CapstoneApplication
import nl.hva.capstone.activities.OngoingCallActivity
import nl.hva.capstone.api.ClientEvent
import nl.hva.capstone.api.model.CallResponse
import nl.hva.capstone.api.model.WebRTCPayload
import nl.hva.capstone.api.model.input.IncomingCallOffer
import nl.hva.capstone.api.model.output.Conversation
import nl.hva.capstone.api.model.output.OutgoingCallOffer

class CallViewModel(
  private val application: CapstoneApplication,
) : AndroidViewModel(application) {
  private val scope = CoroutineScope(Dispatchers.IO)
  private val sessionVM = application.sessionVM
  private val conversationsVM = sessionVM.conversationsVM

  val callState = MutableLiveData<CallState>(CallState.None)

  fun call(conversation: Conversation) {
    val offer = IncomingCallOffer(conversation.otherParticipant.id, conversation.id)
    val event = ClientEvent.CallOfferEvent(offer)
    sessionVM.websocket.sendMessage(event)

    callState.value = CallState.Ringing.Outgoing(conversation.id)

    val intent = Intent(application, OngoingCallActivity::class.java)
    application.mainActivity!!.startActivity(intent)
  }

  fun onCallOffer(callOffer: OutgoingCallOffer) {
    callState.postValue(CallState.Ringing.Incoming(callOffer.conversationID))
  }

  fun onCallResponse(data: CallResponse) {
    scope.launch {
      delay(1000)
      callState.postValue(CallState.Connected)
    }
  }

  fun acceptCall() {
    val state = callState.value
    Log.v("CallViewModel", "accepting $state")
    if (state !is CallState.Ringing.Incoming) return

    val callerID = conversationsVM.conversations.value!!
      .find { it.id == state.conversationID }!!
      .otherParticipant.id

    val response = CallResponse(callerID, accepted = true)
    val event = ClientEvent.CallResponseEvent(response)
    sessionVM.websocket.sendMessage(event)

    callState.value = CallState.Connected
  }

  fun onWebRTCPayload(data: WebRTCPayload) {
    application.signalingClient.onWebRTCPayload(data.payload)
  }
}