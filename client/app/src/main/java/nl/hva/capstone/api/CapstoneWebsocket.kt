package nl.hva.capstone.api

import android.util.Log
import kotlinx.serialization.json.Json
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString

abstract class CapstoneWebsocket : WebSocketListener() {
  private var ws: WebSocket? = null
  private val TAG = "CapstoneWebsocket"
  private val serializer = Json {
    ignoreUnknownKeys = true
  }

  fun start(userID: Int) {
    ws = CapstoneApi.createWebSocket(userID, this)
  }

  abstract fun onMessage(event: ServerEvent)

  override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
    val raw = bytes.utf8()
    val decoded = serializer.decodeFromString(ServerEvent, raw)
    onMessage(decoded)
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