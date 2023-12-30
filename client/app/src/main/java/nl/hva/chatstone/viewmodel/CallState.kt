package nl.hva.chatstone.viewmodel

sealed class CallState() {
  sealed class Ringing(val conversationID: Int) : CallState() {
    data class Incoming(val id: Int) : Ringing(id)
    data class Outgoing(val id: Int) : Ringing(id)
  }
  object Connected : CallState()
  object None : CallState()
}
