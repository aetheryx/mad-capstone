package nl.hva.capstone.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import nl.hva.capstone.data.api.CapstoneApi
import nl.hva.capstone.data.api.model.AuthCredentials
import nl.hva.capstone.data.api.model.User

enum class SessionState {
  INITIALISING,
  STALE,
  LOGGING_IN,
  CREDENTIAL_ERROR,
  READY
}

class SessionViewModel(application: Application) : AndroidViewModel(application) {
  private var token: String? = null
  private var capstoneApi = CapstoneApi.createApi("")
  private val scope = CoroutineScope(Dispatchers.IO)

  val state = MutableLiveData(SessionState.STALE)
  val me: MutableLiveData<User> = MutableLiveData()

  fun logIn(username: String, password: String) {
    state.value = SessionState.LOGGING_IN

    scope.launch {
      try {
        val resp = capstoneApi.logIn(AuthCredentials(username, password))
        token = resp.token
        capstoneApi = CapstoneApi.createApi(token!!)
        state.postValue(SessionState.READY)
      } catch (err: Exception) {
        state.postValue(SessionState.CREDENTIAL_ERROR)
      }
    }
  }

  fun getMe() {
    scope.launch {
      val user = capstoneApi.getMe()
      me.postValue(user)
    }
  }
}