package nl.hva.chatstone.viewmodel

sealed class CallState {
  sealed class Ringing(
    val conversationID: Int,
    val callID: Int
  ) : CallState() {
    class Incoming(
      conversationID: Int,
      callID: Int
    ) : Ringing(conversationID, callID)

    class Outgoing(
      conversationID: Int,
      callID: Int
    ) : Ringing(conversationID, callID)

    class Declined(
      conversationID: Int,
      callID: Int
    ) : Ringing(conversationID, callID)
  }

  data class Connected(val callID: Int) : CallState()
  object None : CallState()
}
