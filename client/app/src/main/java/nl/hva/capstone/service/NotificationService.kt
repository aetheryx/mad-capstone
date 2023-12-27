package nl.hva.capstone.service

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Process
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.android.asCoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import nl.hva.capstone.api.CapstoneApi
import nl.hva.capstone.dataStore
import nl.hva.capstone.viewmodel.sessionTokenKey

class NotificationService : Service() {
  private val TAG = "NotificationService"
  private var ws: NotificationWebsocket? = null

  override fun onCreate() {
    val handlerThread = HandlerThread("notificationservice_t", Process.THREAD_PRIORITY_BACKGROUND)
    handlerThread.start()

    val handler = Handler(handlerThread.looper)
    val scope = CoroutineScope(handler.asCoroutineDispatcher())

    scope.launch {
      openSocket()
    }
  }

  override fun onBind(intent: Intent?): IBinder? {
    return null
  }

  private suspend fun openSocket() {
    try {
      val data = dataStore.data.first()
      val token = data[sessionTokenKey]!!

      val capstoneApi = CapstoneApi.createApi(token)

      val user = capstoneApi.getMe()
      ws = NotificationWebsocket(user.id, this)
    } catch (err: Exception) {
      Log.v(TAG, "$err ${err.stackTraceToString()}")
    }
  }
}