package nl.hva.chatstone.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import nl.hva.chatstone.ChatstoneApplication
import nl.hva.chatstone.api.model.input.CreateMessageInput
import nl.hva.chatstone.api.model.output.Conversation
import nl.hva.chatstone.api.model.output.ConversationMessage

class MessagesViewModel(
  val application: ChatstoneApplication
): AndroidViewModel(application) {
  private val sessionVM = application.sessionVM
  private val conversationsVM = sessionVM.conversationsVM

  private val scope = CoroutineScope(Dispatchers.IO)
  private val chatstoneApi get() = sessionVM.chatstoneApi

  val messages = HashMap<Int, SnapshotStateList<ConversationMessage>>()

  fun addConversationMessage(message: ConversationMessage) {
    val messages = messages[message.conversationID] ?: return
    messages.add(0, message)

    val conversation = conversationsVM.getConversation(message.conversationID) ?: return
    val newConversation = conversation.copy(lastMessage = message)
    conversationsVM.updateConversation(newConversation)
  }

  fun sendMessage(conversation: Conversation, content: String) {
    scope.launch {
      val input = CreateMessageInput(content)
      val message = chatstoneApi.createMessage(conversation.id, input)
      addConversationMessage(message)
    }
  }

  suspend fun fetchConversationMessages(conversation: Conversation) {
    val data = mutableStateListOf<ConversationMessage>()
    messages[conversation.id] = data

    val messages = chatstoneApi.getConversationMessages(
      conversationID = conversation.id,
      limit = 50,
      offset = 0
    )
    data.addAll(messages)
  }
}