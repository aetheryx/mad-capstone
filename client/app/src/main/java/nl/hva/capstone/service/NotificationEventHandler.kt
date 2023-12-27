package nl.hva.capstone.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import nl.hva.capstone.CapstoneApplication
import nl.hva.capstone.R
import nl.hva.capstone.api.ServerEvent
import nl.hva.capstone.api.model.output.ConversationMessage

enum class NotificationChannelID {
  Message,
  Call
}

class NotificationEventHandler(
  private val application: CapstoneApplication,
) {
  private val scope = CoroutineScope(Dispatchers.IO)
  private val sessionVM = application.sessionVM
  private val conversationVM = sessionVM.conversationsVM

  init {
    scope.launch {
      sessionVM.websocket.start(conversationVM.me.id)
      sessionVM.websocket.websocketEvents.collect {
        onServerEvent(it)
      }
    }
  }

  private fun onServerEvent(event: ServerEvent) {
    when (event) {
      is ServerEvent.MessageCreateEvent -> handleMessageCreate(event.data)
      else -> {}
    }
  }

  private fun handleMessageCreate(message: ConversationMessage) {
    if (application.activityIsOpen) return

    val manager = application.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    val channelID = NotificationChannelID.Message.toString()
    val channel = NotificationChannel(channelID, channelID, NotificationManager.IMPORTANCE_HIGH)
    manager.createNotificationChannel(channel)

    val title = conversationVM.conversations.value!!
      .find { it.id == message.conversationID }!!
      .otherParticipant.username

    val notification = NotificationCompat.Builder(application, channelID)
        .setContentTitle(title)
        .setContentText(message.content)
        .setSmallIcon(R.drawable.default_pfp)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .build()

    manager.notify(message.id, notification)
  }
}