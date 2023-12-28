package nl.hva.capstone.api

import android.util.Log
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.serialization.json.Json
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString

class CapstoneWebsocket : WebSocketListener() {
  private var ws: WebSocket? = null
  private val TAG = "CapstoneWebsocket"
  private val serializer = Json {
    ignoreUnknownKeys = true
  }

  val websocketEvents = MutableSharedFlow<ServerEvent>(
    replay = 0,
    extraBufferCapacity = 50
  )

  fun start(userID: Int) {
    synchronized(this) {
      if (ws == null) {
        ws = CapstoneApi.createWebSocket(userID, this)
      }
    }
  }

  override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
    val raw = bytes.utf8()
    val event = serializer.decodeFromString(ServerEvent, raw)
    websocketEvents.tryEmit(event)
  }

  fun sendMessage(event: ClientEvent) {
    val text = event.toJSON().toString()
    Log.v(TAG, "sending $text")
    ws!!.send(text)
  }

  override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
    Log.v(TAG, "closed")
  }

  override fun onOpen(webSocket: WebSocket, response: Response) {
    Log.v(TAG, "open")
  }

  override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
    Log.v(TAG, "errored")
    t.printStackTrace()
  }
}