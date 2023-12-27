package nl.hva.capstone.viewmodel

import nl.hva.capstone.api.model.output.Conversation

sealed class CallState() {
  sealed class Ringing(val conversationID: Int) : CallState() {
    data class Incoming(val id: Int) : Ringing(id)
    data class Outgoing(val id: Int) : Ringing(id)
  }
  class Connected() : CallState()
  class None(): CallState()
}
