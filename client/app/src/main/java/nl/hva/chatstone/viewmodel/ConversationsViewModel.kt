package nl.hva.chatstone.viewmodel

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import nl.hva.chatstone.ChatstoneApplication
import nl.hva.chatstone.api.model.input.CreateConversationInput
import nl.hva.chatstone.api.model.output.Conversation

sealed class ConversationCreateState(val id: Int?) {
  class None : ConversationCreateState(null)
  class Creating : ConversationCreateState(null)
  class Created(id: Int) : ConversationCreateState(id)
  class Errored : ConversationCreateState(null)
}

class ConversationsViewModel(
  private val application: ChatstoneApplication,
) : AndroidViewModel(application) {
  private val TAG = "ConversationsViewModel"
  val sessionVM = application.sessionVM
  val messagesVM by lazy { MessagesViewModel(application) }
  private val chatstoneApi get() = sessionVM.chatstoneApi

  private val scope = CoroutineScope(Dispatchers.IO)
  private val handler = CoroutineExceptionHandler { _, throwable ->
    Log.v(TAG, "Caught exception: $throwable")
  }

  val me get() = sessionVM.me.value!!

  val conversations = mutableStateListOf<Conversation>()
  val createState = MutableLiveData<ConversationCreateState>(ConversationCreateState.None())

  fun createConversation(username: String) {
    createState.value = ConversationCreateState.Creating()
    scope.launch(handler) {
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

  fun getConversation(id: Int) =
    conversations.find { it.id == id }

  fun addConversation(conversation: Conversation) {
    conversations.add(conversation)
    messagesVM.messages[conversation.id] = mutableStateListOf()
  }

  fun updateConversation(conversation: Conversation) {
    val index = conversations.indexOfFirst { it.id == conversation.id }
    conversations[index] = conversation
  }

  fun deleteConversation(conversation: Conversation) = scope.launch(handler) {
    chatstoneApi.deleteConversation(conversation.id)
  }

  fun onDeleteConversation(id: Int) {
    val idx = conversations.indexOfFirst { it.id == id }
    conversations.removeAt(idx)
    messagesVM.messages[id]?.clear()
  }

  fun fetchConversations() = scope.launch(handler) {
    val fullConversations = chatstoneApi.getConversations()
    conversations.clear()
    conversations.addAll(fullConversations)

    fullConversations.forEach {
      messagesVM.fetchConversationMessages(it)
    }
  }
}