package nl.hva.chatstone.viewmodel

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.firebase.Firebase
import com.google.firebase.storage.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import nl.hva.chatstone.BuildConfig
import nl.hva.chatstone.ChatstoneApplication
import nl.hva.chatstone.api.ChatstoneApi
import nl.hva.chatstone.api.ChatstoneWebsocket
import nl.hva.chatstone.api.ServerEvent
import nl.hva.chatstone.api.model.input.LoginInput
import nl.hva.chatstone.api.model.input.SignupInput
import nl.hva.chatstone.api.model.output.User

enum class SessionState {
  INITIALISING,
  LOGGING_IN,
  STALE,
  CREDENTIAL_ERROR,
  READY
}

val Context.sessionDataStore by preferencesDataStore(name = "session")
val sessionTokenKey = stringPreferencesKey("session_token")

class SessionViewModel(val application: ChatstoneApplication) : AndroidViewModel(application) {
  var chatstoneApi = ChatstoneApi.createApi("")
  private val scope = CoroutineScope(Dispatchers.IO)
  private val storage = Firebase.storage("gs://${BuildConfig.FIREBASE_BUCKET}")
  private var listening = false

  val targetURL = mutableStateOf<String?>(null)

  val conversationsVM by lazy { ConversationsViewModel(application) }
  val messagesVM by lazy { conversationsVM.messagesVM }
  val callVM by lazy { CallViewModel(application) }

  val websocket = ChatstoneWebsocket()

  val state = MutableLiveData(SessionState.INITIALISING)
  val me: MutableLiveData<User> = MutableLiveData()

  init {
    scope.launch {
      val data = application.sessionDataStore.data.first()
      val isValid = data[sessionTokenKey]?.let { onReady(it) } ?: false
      if (!isValid) {
        state.postValue(SessionState.STALE)
      }
    }
  }

  fun logIn(username: String, password: String) {
    state.value = SessionState.LOGGING_IN

    scope.launch {
      val isValid = try {
        val resp = chatstoneApi.logIn(LoginInput(username, password))
        onReady(resp.token)
      } catch (err: Exception) {
        false
      }

      if (!isValid) {
        state.postValue(SessionState.CREDENTIAL_ERROR)
      }
    }
  }

  fun signUp(username: String, password: String, mediaURI: Uri) {
    state.value = SessionState.LOGGING_IN

    scope.launch {
      val ref = storage.reference.child("profile_pictures/$username.png")

      val imageURI = ref.putFile(mediaURI)
        .continueWithTask { ref.downloadUrl }
        .addOnCompleteListener { it.result }
        .await()
        .toString()
        .split("/o/")[1]
        .let { Uri.encode(it) }

      val signupInput = SignupInput(username, password, imageURI)
      val resp = chatstoneApi.signUp(signupInput)
      onReady(resp.token)
    }
  }

  fun signOut() = scope.launch {
    application.sessionDataStore.edit { it.remove(sessionTokenKey) }
    state.postValue(SessionState.STALE)
    me.postValue(null)
    websocket.destroy()
  }

  private suspend fun onReady(newToken: String): Boolean {
    chatstoneApi = ChatstoneApi.createApi(newToken)

    val user = try {
      chatstoneApi.getMe()
    } catch (err: Exception) {
      return false
    }

    application.sessionDataStore.edit { it[sessionTokenKey] = newToken }
    me.postValue(user)
    state.postValue(SessionState.READY)

    websocket.start(user.id)
    conversationsVM.fetchConversations()

    return true
  }

  fun listenForEvents() = scope.launch {
    synchronized(this) {
      if (listening) return@launch
      listening = true
    }

    websocket.websocketEvents.collect { event ->
      when (event) {
        is ServerEvent.MessageCreateEvent ->
          messagesVM.addConversationMessage(event.data)

        is ServerEvent.ConversationCreateEvent ->
          conversationsVM.addConversation(event.data)

        is ServerEvent.ConversationDeleteEvent ->
          conversationsVM.onDeleteConversation(event.data)

        is ServerEvent.CallResponseEvent ->
          callVM.onCallResponse(event.data)

        is ServerEvent.CallHangUpEvent ->
          callVM.onCallHangUp()

        is ServerEvent.WebRTCPayloadEvent ->
          callVM.onWebRTCPayload(event.data)

        else -> Unit
      }
    }
  }
}

