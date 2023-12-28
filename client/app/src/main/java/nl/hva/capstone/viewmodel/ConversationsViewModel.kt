package nl.hva.capstone.viewmodel

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import nl.hva.capstone.CapstoneApplication
import nl.hva.capstone.api.model.output.Conversation
import nl.hva.capstone.api.model.output.ConversationMessage
import nl.hva.capstone.api.model.input.CreateConversationInput
import nl.hva.capstone.api.model.input.CreateMessageInput

sealed class ConversationCreateState(val id: Int?) {
  class None: ConversationCreateState(null)
  class Creating: ConversationCreateState(null)
  class Created(id: Int): ConversationCreateState(id)
  class Errored: ConversationCreateState(null)
}

class ConversationsViewModel(
  private val application: CapstoneApplication,
) : AndroidViewModel(application) {
  val sessionVM = application.sessionVM
  private val scope = CoroutineScope(Dispatchers.IO)
  private val capstoneApi get() = sessionVM.capstoneApi

  val me get() = sessionVM.me.value!!

  val conversations = MutableLiveData<List<Conversation>>()
  val conversationMessages = HashMap<Int, SnapshotStateList<ConversationMessage>>()
  val createState = MutableLiveData<ConversationCreateState>(ConversationCreateState.None())

  fun createConversation(username: String) {
    createState.value = ConversationCreateState.Creating()
    scope.launch {
      try {
        val user = capstoneApi.findUser(username)
        val conversation = capstoneApi.createConversation(CreateConversationInput(user.id))
        conversations.postValue(capstoneApi.getConversations())
        createState.postValue(ConversationCreateState.Created(conversation.id))
      } catch (err: Exception) {
        Log.v("conversationvm", "failed to find user $username $err")
        createState.postValue(ConversationCreateState.Errored())
      }
    }
  }

  fun addConversationMessage(message: ConversationMessage) {
    val messages = conversationMessages[message.conversationID] ?: return
    messages.add(0, message)
  }

  fun sendMessage(conversation: Conversation, content: String) {
    scope.launch {
      val input = CreateMessageInput(content)
      val message = capstoneApi.createMessage(conversation.id, input)
      addConversationMessage(message)
    }
  }

  fun fetchConversations() {
    scope.launch {
      val fullConversations = capstoneApi.getConversations()
      conversations.postValue(fullConversations)

      fullConversations.forEach { fetchConversationMessages(it) }
    }
  }

  private suspend fun fetchConversationMessages(conversation: Conversation) {
    val data = mutableStateListOf<ConversationMessage>()
    conversationMessages[conversation.id] = data

    val messages = capstoneApi.getConversationMessages(
      conversationID = conversation.id,
      limit = 50,
      offset = 0
    )
    data.addAll(messages)
  }
}