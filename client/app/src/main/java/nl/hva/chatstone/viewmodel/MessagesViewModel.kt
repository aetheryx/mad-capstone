package nl.hva.chatstone.viewmodel

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import nl.hva.chatstone.ChatstoneApplication
import nl.hva.chatstone.api.model.input.CreateMessageInput
import nl.hva.chatstone.api.model.output.Conversation
import nl.hva.chatstone.api.model.output.ConversationMessage

class MessagesViewModel(
  val application: ChatstoneApplication
) : AndroidViewModel(application) {
  private val TAG = "MessagesViewModel"
  private val sessionVM = application.sessionVM
  private val conversationsVM = sessionVM.conversationsVM

  private val chatstoneApi get() = sessionVM.chatstoneApi
  private val scope = CoroutineScope(Dispatchers.IO)
  private val handler = CoroutineExceptionHandler { _, throwable ->
    Log.v(TAG, "Caught exception: $throwable")
  }

  val messages = HashMap<Int, SnapshotStateList<ConversationMessage>>()
  val messageReply = MutableLiveData<ConversationMessage>()

  fun addConversationMessage(message: ConversationMessage) {
    populateMessage(message)
    val messages = messages[message.conversationID] ?: return
    messages.add(0, message)

    val conversation = conversationsVM.getConversation(message.conversationID) ?: return
    val newConversation = conversation.copy(lastMessage = message)
    conversationsVM.updateConversation(newConversation)
  }

  fun sendMessage(conversation: Conversation, content: String) {
    scope.launch(handler) {
      val input = CreateMessageInput(
        content = content,
        replyToId = messageReply.value?.id
      )

      val message = chatstoneApi.createMessage(conversation.id, input)
      addConversationMessage(message)
      messageReply.postValue(null)
    }
  }

  suspend fun fetchConversationMessages(conversation: Conversation) {
    val data = mutableStateListOf<ConversationMessage>()
    messages[conversation.id] = data

    val messages = chatstoneApi.getConversationMessages(
      conversationID = conversation.id,
      limit = 200,
      offset = 0
    )

    data.addAll(messages)
    messages.forEach { populateMessage(it) }
  }

  private fun populateMessage(message: ConversationMessage) {
    message.isAuthor = message.authorID == conversationsVM.me.id

    if (message.replyToId != null) {
      val channelMessages = messages[message.conversationID]
      val replyTo = channelMessages?.find { it.id == message.replyToId }
      message.replyTo = replyTo
    }
  }
}