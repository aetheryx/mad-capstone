package nl.hva.chatstone.service

import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.annotation.StringRes
import androidx.core.content.getSystemService
import nl.hva.chatstone.ChatstoneApplication
import nl.hva.chatstone.R

enum class Channels(
  @StringRes val channelName: Int,
  val id: String,
) {
  Messages(R.string.messages, "chatstone_messages"),
  Calls(R.string.calls, "chatstone_calls")
}


class NotificationChannelManager(
  private val application: ChatstoneApplication,
) {
  private val ctx = application.applicationContext
  private val notificationManager = ctx.getSystemService<NotificationManager>()!!

  fun createMessagesChannel(): NotificationChannel {
    val values = Channels.Messages
    val channel = NotificationChannel(values.id, ctx.getString(values.channelName), NotificationManager.IMPORTANCE_HIGH)

    notificationManager.createNotificationChannel(channel)
    return channel
  }

  fun createCallChannel(): NotificationChannel {
    val values = Channels.Calls
    val channel = NotificationChannel(values.id, ctx.getString(values.channelName), NotificationManager.IMPORTANCE_HIGH)
    notificationManager.createNotificationChannel(channel)

    return channel
  }
}