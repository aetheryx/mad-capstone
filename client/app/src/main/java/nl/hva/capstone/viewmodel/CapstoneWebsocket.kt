package nl.hva.capstone.viewmodel

import android.util.Log
import kotlinx.serialization.json.Json
import nl.hva.capstone.api.CapstoneApi
import nl.hva.capstone.api.WebsocketEvent
import nl.hva.capstone.api.model.output.ConversationMessage
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString

class CapstoneWebsocket(
  private val sessionVM: SessionViewModel,
) : WebSocketListener() {
  private val TAG = "CapstoneWebsocket"
  private val conversationsVM get() = sessionVM.conversationsVM
  private val decoder = Json {
    ignoreUnknownKeys = true
  }

  fun connect() {
    val ws = CapstoneApi.createWebSocket(conversationsVM.me.id, this)
  }

  private fun onMessageCreate(message: ConversationMessage) {
    val messages = conversationsVM.conversationMessages[message.conversationID]!!
    val newMessages = messages.value!!.plus(message)
    messages.postValue(ArrayList(newMessages))  // TODO: perf
  }

  override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
    val raw = bytes.utf8()
    val decoded = decoder.decodeFromString(WebsocketEvent, raw)

    when (decoded) {
      is WebsocketEvent.MessageCreate -> onMessageCreate(decoded.data)
    }
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

