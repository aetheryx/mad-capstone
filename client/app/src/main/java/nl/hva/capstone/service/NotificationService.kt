package nl.hva.capstone.service

import android.os.Handler
import android.os.HandlerThread
import android.os.Process
import androidx.lifecycle.LifecycleService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.android.asCoroutineDispatcher
import kotlinx.coroutines.launch
import nl.hva.capstone.CapstoneApplication
import nl.hva.capstone.viewmodel.SessionState

class NotificationService : LifecycleService() {
  private val TAG = "NotificationService"
  private var ws: NotificationEventHandler? = null

  override fun onCreate() {
    val sessionVM = (application as CapstoneApplication).sessionVM
    sessionVM.state.observe(this) {
      if (it == SessionState.INITIALISING) return@observe
      if (it == SessionState.READY) initSocket()

      sessionVM.state.removeObservers(this)
    }

    super.onCreate()
  }

  private fun initSocket() {
    val handlerThread = HandlerThread("notificationservice_t", Process.THREAD_PRIORITY_BACKGROUND)
    handlerThread.start()

    val handler = Handler(handlerThread.looper)
    val scope = CoroutineScope(handler.asCoroutineDispatcher())

    scope.launch {
      ws = NotificationEventHandler(application as CapstoneApplication)
    }
  }
}