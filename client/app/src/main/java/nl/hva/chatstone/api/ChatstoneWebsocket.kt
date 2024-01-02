package nl.hva.chatstone.api

import android.util.Log
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString

class ChatstoneWebsocket : WebSocketListener() {
  private val scope = CoroutineScope(Dispatchers.IO)
  private var ws: WebSocket? = null
  private var userID: Int? = null
  private val TAG = "ChatstoneWebsocket"
  private val serializer = Json {
    ignoreUnknownKeys = true
  }

  val reconnecting = MutableLiveData(false)

  val websocketEvents = MutableSharedFlow<ServerEvent>(
    replay = 0,
    extraBufferCapacity = 50
  )

  fun start(userID: Int) {
    this.userID = userID
    Log.v(TAG, "starting")
    synchronized(this) {
      if (ws == null) {
        ws = ChatstoneApi.createWebSocket(userID, this)
      }
    }
  }

  override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
    val raw = bytes.utf8()
    val event = serializer.decodeFromString(ServerEvent, raw)
    Log.v(TAG, "received: $event")
    websocketEvents.tryEmit(event)
  }

  fun sendMessage(event: ClientEvent) {
    val text = event.toJSON().toString()
    Log.v(TAG, "sending $text")
    ws!!.send(text)
  }

  fun destroy() {
    synchronized(this) {
      Log.v(TAG, "destroying")
      runCatching { ws?.cancel() }
      ws = null
    }
  }

  private fun reconnect() {
    synchronized(this) {
      scope.launch {
        reconnecting.postValue(true)
        destroy()
        delay(1000)
        ws = ChatstoneApi.createWebSocket(userID!!, this@ChatstoneWebsocket)
      }
    }
  }

  override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
    Log.v(TAG, "received closing event: $code, $reason")
  }

  override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
    Log.v(TAG, "received close event: $code, $reason")
    reconnect()
  }

  override fun onOpen(webSocket: WebSocket, response: Response) {
    reconnecting.postValue(false)
    Log.v(TAG, "open")
  }

  override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
    Log.v(TAG, "failure: ${t.stackTraceToString()} ${response}")
    reconnect()
  }
}