package nl.hva.chatstone.viewmodel

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import nl.hva.chatstone.ChatstoneApplication
import nl.hva.chatstone.api.model.output.Conversation
import nl.hva.chatstone.api.model.output.ConversationMessage
import nl.hva.chatstone.api.model.input.CreateConversationInput
import nl.hva.chatstone.api.model.input.CreateMessageInput

sealed class ConversationCreateState(val id: Int?) {
  class None: ConversationCreateState(null)
  class Creating: ConversationCreateState(null)
  class Created(id: Int): ConversationCreateState(id)
  class Errored: ConversationCreateState(null)
}

class ConversationsViewModel(
  private val application: ChatstoneApplication,
) : AndroidViewModel(application) {
  val sessionVM = application.sessionVM
  private val scope = CoroutineScope(Dispatchers.IO)
  private val chatstoneApi get() = sessionVM.chatstoneApi

  val me get() = sessionVM.me.value!!

  val conversations = MutableLiveData<List<Conversation>>()
  val conversationMessages = HashMap<Int, SnapshotStateList<ConversationMessage>>()
  val createState = MutableLiveData<ConversationCreateState>(ConversationCreateState.None())

  fun createConversation(username: String) {
    createState.value = ConversationCreateState.Creating()
    scope.launch {
      try {
        val user = chatstoneApi.findUser(username)
        val conversation = chatstoneApi.createConversation(CreateConversationInput(user.id))
        createState.postValue(ConversationCreateState.Created(conversation.id))

        addConversation(conversation)
      } catch (err: Exception) {
        Log.v("conversationvm", "failed to find user $username $err")
        createState.postValue(ConversationCreateState.Errored())
      }
    }
  }

  fun addConversationMessage(message: ConversationMessage) {
    val messages = conversationMessages[message.conversationID] ?: return
    messages.add(0, message)

    val newConversations = ArrayList(conversations.value!!)
    val conversation = newConversations.find { it.id == message.conversationID }!!
    val index = newConversations.indexOf(conversation)

    val newConversation = conversation.copy(lastMessage = message)
    newConversations[index] = newConversation
    conversations.postValue(newConversations)
  }

  fun addConversation(conversation: Conversation) {
    conversationMessages[conversation.id] = mutableStateListOf()
    val newConversations = conversations.value!!.plus(conversation)
    conversations.postValue(newConversations)
  }

  fun deleteConversation(conversation: Conversation) = scope.launch {
    chatstoneApi.deleteConversation(conversation.id)
  }

  fun onDeleteConversation(id: Int) = scope.launch {
    sessionVM.targetURL.value = "/conversations"
    delay(50)

    val newConversations = conversations.value!!.filter { it.id != id }
    conversations.postValue(newConversations)
    conversationMessages[id]!!.clear()
  }

  fun sendMessage(conversation: Conversation, content: String) {
    scope.launch {
      val input = CreateMessageInput(content)
      val message = chatstoneApi.createMessage(conversation.id, input)
      addConversationMessage(message)
    }
  }

  fun fetchConversations() {
    scope.launch {
      val fullConversations = chatstoneApi.getConversations()
      conversations.postValue(fullConversations)

      fullConversations.forEach { fetchConversationMessages(it) }
    }
  }

  private suspend fun fetchConversationMessages(conversation: Conversation) {
    val data = mutableStateListOf<ConversationMessage>()
    conversationMessages[conversation.id] = data

    val messages = chatstoneApi.getConversationMessages(
      conversationID = conversation.id,
      limit = 50,
      offset = 0
    )
    data.addAll(messages)
  }
}