package nl.hva.capstone.viewmodel

import nl.hva.capstone.api.ServerEvent
import nl.hva.capstone.api.model.CallResponse
import nl.hva.capstone.api.model.output.ConversationMessage
import nl.hva.capstone.api.model.output.OutgoingCallOffer

class ConversationsEventHandler(private val conversationsVM: ConversationsViewModel) {
  suspend fun collect() {
    conversationsVM.sessionVM.websocket.websocketEvents.collect {
      onServerEvent(it)
    }
  }

  private fun onServerEvent(event: ServerEvent) {
    when (event) {
      is ServerEvent.MessageCreateEvent -> onMessageCreate(event.data)
      is ServerEvent.CallOfferEvent -> onCallOffer(event.data)
      is ServerEvent.CallResponseEvent -> onCallResponse(event.data)
      is ServerEvent.WebRTCPayloadEvent -> {} // signalingClient?.onWebRTCPayload(decoded.data.payload)
    }
  }

  private fun onMessageCreate(message: ConversationMessage) {
    conversationsVM.addConversationMessage(message)
  }

  private fun onCallOffer(offer: OutgoingCallOffer) {
    conversationsVM.callState.postValue(CallState.Ringing.Incoming(offer.conversationID))
  }

  private fun onCallResponse(response: CallResponse) {
    val callState = if (response.accepted) CallState.Connected() else CallState.None()
    conversationsVM.callState.postValue(callState)
  }
}