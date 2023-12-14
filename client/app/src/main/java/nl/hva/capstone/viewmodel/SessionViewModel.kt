package nl.hva.capstone.viewmodel

import android.app.Application
import android.net.Uri
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.firebase.Firebase
import com.google.firebase.storage.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import nl.hva.capstone.data.api.CapstoneApi
import nl.hva.capstone.data.api.model.LoginInput
import nl.hva.capstone.data.api.model.SignupInput
import nl.hva.capstone.data.api.model.User
import nl.hva.capstone.dataStore

enum class SessionState {
  INITIALISING,
  LOGGING_IN,
  STALE,
  CREDENTIAL_ERROR,
  READY
}

val sessionTokenKey = stringPreferencesKey("session_token")

class SessionViewModel(application: Application) : AndroidViewModel(application) {
  private var token: String? = null
  var capstoneApi = CapstoneApi.createApi("")
  private val scope = CoroutineScope(Dispatchers.IO)
  private val dataStore = application.dataStore
  private val storage = Firebase.storage("gs://capstone-386f7.appspot.com") // TODO: move to env
  val conversationsViewModel = ConversationsViewModel(application, this)

  val state = MutableLiveData(SessionState.INITIALISING)
  val me: MutableLiveData<User> = MutableLiveData()

  init {
    scope.launch {
      val data = dataStore.data.first()
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
        .addOnCompleteListener { it.result }
        .await()

      val signupInput = SignupInput(username, password, imageURI.toString())
      val resp = capstoneApi.signUp(signupInput)
      onReady(resp.token)
    }
  }

  private suspend fun onReady(newToken: String): Boolean {
    token = newToken
    capstoneApi = CapstoneApi.createApi(newToken)

    val user = try {
      capstoneApi.getMe()
    } catch (err: Exception) {
      return false
    }

    dataStore.edit { it[sessionTokenKey] = newToken }
    state.postValue(SessionState.READY)
    me.postValue(user)

    return true
  }
}

