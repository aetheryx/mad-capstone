package nl.hva.chatstone.service

import android.os.Handler
import android.os.HandlerThread
import android.os.Process
import androidx.lifecycle.LifecycleService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.android.asCoroutineDispatcher
import kotlinx.coroutines.launch
import nl.hva.chatstone.ChatstoneApplication
import nl.hva.chatstone.viewmodel.SessionState

class ChatstoneService : LifecycleService() {
  private val TAG = "ChatstoneService"
  private var ws: ChatstoneEventHandler? = null

  override fun onCreate() {
    val sessionVM = (application as ChatstoneApplication).sessionVM
    sessionVM.state.observe(this) {
      if (it == SessionState.INITIALISING) return@observe
      if (it == SessionState.READY) initSocket()

      sessionVM.state.removeObservers(this)
    }

    super.onCreate()
  }

  private fun initSocket() {
    val handlerThread = HandlerThread("chatstoneservice_t", Process.THREAD_PRIORITY_BACKGROUND)
    handlerThread.start()

    val handler = Handler(handlerThread.looper)
    val scope = CoroutineScope(handler.asCoroutineDispatcher())

    scope.launch {
      ws = ChatstoneEventHandler(application as ChatstoneApplication)
    }
  }
}