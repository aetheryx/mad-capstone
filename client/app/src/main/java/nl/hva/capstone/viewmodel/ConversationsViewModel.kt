package nl.hva.capstone.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import nl.hva.capstone.data.api.Conversation
import nl.hva.capstone.data.api.FullConversation
import nl.hva.capstone.data.api.model.ConversationMessage
import nl.hva.capstone.data.api.model.CreateConversation

sealed class ConversationCreateState(val id: Int?) {
  class None: ConversationCreateState(null)
  class Creating: ConversationCreateState(null)
  class Created(id: Int): ConversationCreateState(id)
  class Errored: ConversationCreateState(null)
}

class ConversationsViewModel(
  application: Application,
  private val sessionViewModel: SessionViewModel
) : AndroidViewModel(application) {
  private val capstoneApi get() = sessionViewModel.capstoneApi
  private val scope = CoroutineScope(Dispatchers.IO)

  val conversations = MutableLiveData<List<FullConversation>>()
  val conversationMessages = HashMap<Int, MutableLiveData<List<ConversationMessage>>>()

  val createState = MutableLiveData<ConversationCreateState>(ConversationCreateState.None())

  fun createConversation(username: String) {
    createState.value = ConversationCreateState.Creating()
    scope.launch {
      try {
        val user = capstoneApi.findUser(username)
        val conversation = capstoneApi.createConversation(CreateConversation(user.id))
        conversations.postValue(capstoneApi.getConversations())
        createState.postValue(ConversationCreateState.Created(conversation.id))
      } catch (err: Exception) {
        Log.v("conversationvm", "failed to find user $username $err")
        createState.postValue(ConversationCreateState.Errored())
      }
    }
  }

  fun fetchConversations() {
    scope.launch {
      val fullConversations = capstoneApi.getConversations()
      conversations.postValue(fullConversations)

      fullConversations.forEach { fetchConversationMessages(it.conversation) }
    }
  }

  private suspend fun fetchConversationMessages(conversation: Conversation) {
    val data = MutableLiveData<List<ConversationMessage>>(emptyList())
    conversationMessages[conversation.id] = data

    val messages = capstoneApi.getConversationMessages(
      conversationID = conversation.id,
      limit = 50,
      offset = 0
    )
    data.postValue(messages)
  }
}