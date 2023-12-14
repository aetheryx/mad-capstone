package nl.hva.capstone.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import nl.hva.capstone.data.api.FullConversation
import nl.hva.capstone.data.api.model.CreateConversation

enum class ConversationCreateState {
  None,
  Creating,
  Created,
  Errored
}

class ConversationsViewModel(
  application: Application,
  private val sessionViewModel: SessionViewModel
) : AndroidViewModel(application) {
  private val capstoneApi get() = sessionViewModel.capstoneApi
  private val scope = CoroutineScope(Dispatchers.IO)

  val conversations = MutableLiveData(emptyList<FullConversation>())

  val createState = MutableLiveData(ConversationCreateState.None)

  fun createConversation(username: String) {
    createState.value = ConversationCreateState.Creating
    scope.launch {
      try {
        val user = capstoneApi.findUser(username)
        capstoneApi.createConversation(CreateConversation(user.id))
        conversations.postValue(capstoneApi.getConversations())
        createState.postValue(ConversationCreateState.Created)
      } catch (err: Exception) {
        Log.v("conversationvm", "failed to find user $username $err")
        createState.postValue(ConversationCreateState.Errored)
      }
    }
  }

  fun fetchConversations() {
    scope.launch {
      conversations.postValue(capstoneApi.getConversations())
    }
  }
}