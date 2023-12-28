package nl.hva.capstone.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
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
import nl.hva.capstone.api.CapstoneApi
import nl.hva.capstone.api.model.input.LoginInput
import nl.hva.capstone.api.model.input.SignupInput
import nl.hva.capstone.api.model.output.User
import nl.hva.capstone.BuildConfig
import nl.hva.capstone.CapstoneApplication
import nl.hva.capstone.api.CapstoneWebsocket

enum class SessionState {
  INITIALISING,
  LOGGING_IN,
  STALE,
  CREDENTIAL_ERROR,
  READY
}

val Context.sessionDataStore by preferencesDataStore(name = "session")
val sessionTokenKey = stringPreferencesKey("session_token")

class SessionViewModel(private val application: CapstoneApplication) : AndroidViewModel(application) {
  var capstoneApi = CapstoneApi.createApi("")
  private val scope = CoroutineScope(Dispatchers.IO)
  private val storage = Firebase.storage("gs://${BuildConfig.FIREBASE_BUCKET}")

  val conversationsVM by lazy {
    ConversationsViewModel(application, this)
  }

  val websocket = CapstoneWebsocket()

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
        val resp = capstoneApi.logIn(LoginInput(username, password))
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
        .addOnCompleteListener { it.result }  // TODO: uri encoding
        .await()

      val signupInput = SignupInput(username, password, imageURI.toString())
      val resp = capstoneApi.signUp(signupInput)
      onReady(resp.token)
    }
  }

  private suspend fun onReady(newToken: String): Boolean {
    capstoneApi = CapstoneApi.createApi(newToken)

    val user = try {
      capstoneApi.getMe()
    } catch (err: Exception) {
      return false
    }

    application.sessionDataStore.edit { it[sessionTokenKey] = newToken }
    me.postValue(user)
    state.postValue(SessionState.READY)

    websocket.start(user.id)

    return true
  }
}

