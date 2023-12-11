package nl.hva.capstone.viewmodels

import android.app.Application
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import nl.hva.capstone.data.api.CapstoneApi
import nl.hva.capstone.data.api.model.AuthCredentials
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
  private var capstoneApi = CapstoneApi.createApi("")
  private val scope = CoroutineScope(Dispatchers.IO)
  private val dataStore = application.dataStore

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
        val resp = capstoneApi.logIn(AuthCredentials(username, password))
        onReady(resp.token)
      } catch (err: Exception) {
        false
      }

      if (!isValid) {
        state.postValue(SessionState.CREDENTIAL_ERROR)
      }
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

