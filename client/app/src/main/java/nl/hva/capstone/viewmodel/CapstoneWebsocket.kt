package nl.hva.capstone.viewmodel

import android.util.Log
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import nl.hva.capstone.api.CapstoneApi
import nl.hva.capstone.api.ClientEvent
import nl.hva.capstone.api.ServerEvent
import nl.hva.capstone.api.model.output.ConversationMessage
import nl.hva.capstone.api.model.output.User
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString

class CapstoneWebsocket(
  private val sessionVM: SessionViewModel,
) : WebSocketListener() {
  private val TAG = "CapstoneWebsocket"
  private val conversationsVM get() = sessionVM.conversationsVM
  private val serializer = Json {
    ignoreUnknownKeys = true
  }

  private var ws: WebSocket? = null

  fun connect(user: User) {
    ws = CapstoneApi.createWebSocket(user.id, this)
  }

  private fun onMessageCreate(message: ConversationMessage) {
    conversationsVM.addConversationMessage(message)
  }

  override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
    val raw = bytes.utf8()
    val decoded = serializer.decodeFromString(ServerEvent, raw)

    when (decoded) {
      is ServerEvent.MessageCreateEvent -> onMessageCreate(decoded.data)
      else -> {
        Log.v(TAG, "got $decoded")
      }
    }
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

