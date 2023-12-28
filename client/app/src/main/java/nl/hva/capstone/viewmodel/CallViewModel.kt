package nl.hva.capstone.viewmodel

import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import nl.hva.capstone.CapstoneApplication
import nl.hva.capstone.activities.OngoingCallActivity
import nl.hva.capstone.api.ClientEvent
import nl.hva.capstone.api.model.input.IncomingCallOffer
import nl.hva.capstone.api.model.output.Conversation

class CallViewModel(
  private val application: CapstoneApplication,
) : AndroidViewModel(application) {
//  private val scope = CoroutineScope(Dispatchers.IO)
  private val sessionVM = application.sessionVM

//  val callState = MutableLiveData<CallState>(CallState.None)

  fun call(conversation: Conversation) {
    val offer = IncomingCallOffer(conversation.otherParticipant.id, conversation.id)
    val message = ClientEvent.CallOfferEvent(offer)
    sessionVM.websocket.sendMessage(message)

//    callState.value = CallState.Ringing.Outgoing(conversation.id)

    val intent = Intent(application, OngoingCallActivity::class.java)
    application.mainActivity!!.startActivity(intent)
  }
}