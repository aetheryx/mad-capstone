package nl.hva.chatstone.viewmodel

sealed class CallState() {
  sealed class Ringing() : CallState() {
    data class Incoming(
      val conversationID: Int,
      val callID: Int
    ) : Ringing()

    data class Outgoing(
      val conversationID: Int,
      val declined: Boolean,
      val callID: Int
    ) : Ringing()
  }
  data class Connected(val callID: Int) : CallState()
  object None : CallState()
}
