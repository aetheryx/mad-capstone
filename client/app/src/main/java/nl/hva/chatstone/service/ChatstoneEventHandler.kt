package nl.hva.chatstone.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import nl.hva.chatstone.ChatstoneApplication
import nl.hva.chatstone.R
import nl.hva.chatstone.activities.IncomingCallActivity
import nl.hva.chatstone.activities.MainActivity
import nl.hva.chatstone.activities.OngoingCallActivity
import nl.hva.chatstone.activities.ToURLActivity
import nl.hva.chatstone.api.ServerEvent
import nl.hva.chatstone.api.model.output.ConversationMessage
import nl.hva.chatstone.api.model.output.OutgoingCallOffer
import java.time.Instant

enum class NotificationChannelID {
  Message,
  Call
}

class ChatstoneEventHandler(
  private val application: ChatstoneApplication,
) {
  private val ctx = application.applicationContext
  private val scope = CoroutineScope(Dispatchers.IO)
  private val sessionVM = application.sessionVM
  private val conversationVM = sessionVM.conversationsVM
  private val notificationManager = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
  private val shortcutManager = ctx.getSystemService(Context.SHORTCUT_SERVICE) as ShortcutManager

  init {
    scope.launch {
      sessionVM.listenForEvents()
      sessionVM.websocket.start(conversationVM.me.id)
      sessionVM.websocket.websocketEvents.collect {
        onServerEvent(it)
      }
    }
  }

  private suspend fun onServerEvent(event: ServerEvent) {
    when (event) {
      is ServerEvent.MessageCreateEvent -> handleMessageCreate(event.data)
      is ServerEvent.CallOfferEvent -> handleCallOfferEvent(event.data)
      else -> {}
    }
  }

  private suspend fun handleMessageCreate(message: ConversationMessage) {
    if (application.activityIsOpen) return

    val channelID = NotificationChannelID.Message.toString()
    val channel = NotificationChannel(channelID, channelID, NotificationManager.IMPORTANCE_HIGH)
    notificationManager.createNotificationChannel(channel)

    val user = conversationVM.conversations.value!!
      .find { it.id == message.conversationID }!!
      .otherParticipant

    val icon = user.getIcon(ctx)
    val person = user.toPerson(ctx)

    val shortcutIntent = Intent(application, MainActivity::class.java)
      .setAction("action")

    val notificationIntent = Intent(application, ToURLActivity::class.java)
      .let {
        it.putExtra("target_url", "/conversations/${message.conversationID}")
        PendingIntent.getActivity(ctx, message.id, it, PendingIntent.FLAG_IMMUTABLE)
      }

    val style = Notification.MessagingStyle(person)
      .addMessage(message.content, message.createdAt.toEpochSecond() * 1000L, person)

    val shortcut = ShortcutInfo.Builder(ctx, "shid")
      .setLongLived(true)
      .setIcon(icon)
      .setShortLabel(user.username)
      .setPerson(person)
      .setIntent(shortcutIntent)
      .build()

    shortcutManager.pushDynamicShortcut(shortcut)

    val notification = Notification.Builder(application, channelID)
      .setStyle(style)
      .setSmallIcon(R.drawable.hva)
      .setShortcutId(shortcut.id)
      .setAutoCancel(true)
      .setContentIntent(notificationIntent)
      .build()

    notificationManager.notify(message.id, notification)
  }

  private suspend fun handleCallOfferEvent(callOffer: OutgoingCallOffer) {
    sessionVM.callVM.onCallOffer(callOffer)

    val id = Instant.now().epochSecond.toInt()

    val channelID = NotificationChannelID.Call.toString()
    val channel = NotificationChannel(channelID, channelID, NotificationManager.IMPORTANCE_HIGH)
    notificationManager.createNotificationChannel(channel)

    val user = conversationVM.conversations.value!!
      .find { it.id == callOffer.conversationID }!!
      .otherParticipant

    val person = user.toPerson(ctx)

    val answerIntent = Intent(application, OngoingCallActivity::class.java)
      .let {
        it.putExtra("notification_id", id)
        PendingIntent.getActivity(ctx, id, it, PendingIntent.FLAG_IMMUTABLE)
      }

    val declineIntent = Intent(application, IncomingCallActivity::class.java) // todo: decline call activity
      .let { PendingIntent.getActivity(ctx, 0, it, PendingIntent.FLAG_IMMUTABLE) }

    val notification = Notification.Builder(application, channelID)
      .setSmallIcon(R.drawable.hva)
      .setStyle(Notification.CallStyle.forIncomingCall(person, declineIntent, answerIntent))
      .setFullScreenIntent(answerIntent, true)
      .setCategory(Notification.CATEGORY_CALL)
      .build()

    notificationManager.notify(id, notification)
  }
}