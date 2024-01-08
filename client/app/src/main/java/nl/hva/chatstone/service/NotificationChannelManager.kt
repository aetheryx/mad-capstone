package nl.hva.chatstone.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.media.AudioAttributes
import androidx.annotation.StringRes
import androidx.core.content.getSystemService
import nl.hva.chatstone.ChatstoneApplication
import nl.hva.chatstone.R
import nl.hva.chatstone.util.resourceToURI

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

    val audioUri = resourceToURI(ctx, R.raw.notification)
    val audioAttributes = AudioAttributes.Builder()
      .setUsage(AudioAttributes.USAGE_NOTIFICATION)
      .build()

    channel.setSound(audioUri, audioAttributes)

    notificationManager.createNotificationChannel(channel)
    return channel
  }

  fun createCallChannel(): NotificationChannel {
    val values = Channels.Calls
    val channel = NotificationChannel(values.id, ctx.getString(values.channelName), NotificationManager.IMPORTANCE_HIGH)

    val audioUri = resourceToURI(ctx, R.raw.ringtone)
    val audioAttributes = AudioAttributes.Builder()
      .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
      .build()

    channel.setSound(audioUri, audioAttributes)

    notificationManager.createNotificationChannel(channel)
    return channel
  }
}