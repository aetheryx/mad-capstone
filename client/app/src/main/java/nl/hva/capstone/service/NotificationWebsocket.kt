package nl.hva.capstone.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context.NOTIFICATION_SERVICE
import android.util.Log
import androidx.core.app.NotificationCompat
import nl.hva.capstone.CapstoneApplication
import nl.hva.capstone.R
import nl.hva.capstone.api.CapstoneWebsocket
import nl.hva.capstone.api.ServerEvent
import nl.hva.capstone.api.model.output.ConversationMessage

enum class NotificationChannelID {
  Message,
  Call
}

class NotificationWebsocket(
  userID: Int,
  private val notificationService: NotificationService
) : CapstoneWebsocket() {
  private val TAG = "NotificationWebsocket"

  init {
    super.start(userID)
  }

  override fun onMessage(event: ServerEvent) {
    val application = (notificationService.application as CapstoneApplication)
    if (application.activityIsOpen) return

    when (event) {
      is ServerEvent.MessageCreateEvent -> onMessageCreate(event.data)
      else -> {}
    }
  }

  private fun onMessageCreate(data: ConversationMessage) {
    with(notificationService.applicationContext) {
      val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

      val channelID = NotificationChannelID.Message.toString()
      val channel = NotificationChannel(channelID, channelID, NotificationManager.IMPORTANCE_HIGH)
      manager.createNotificationChannel(channel)

      val notification = NotificationCompat.Builder(this, channelID)
        .setContentTitle("Aaa")
        .setContentText(data.content)
        .setSmallIcon(R.drawable.default_pfp)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .build()

      Log.v(TAG, "sending notification $notification")

      manager.notify(data.id, notification)
    }
  }
}