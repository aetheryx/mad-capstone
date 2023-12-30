package nl.hva.chatstone.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Person
import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.util.Log
import androidx.core.graphics.drawable.toBitmap
import coil.ImageLoader
import coil.request.ImageRequest
import coil.transform.CircleCropTransformation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import nl.hva.chatstone.ChatstoneApplication
import nl.hva.chatstone.R
import nl.hva.chatstone.activities.IncomingCallActivity
import nl.hva.chatstone.activities.MainActivity
import nl.hva.chatstone.activities.OngoingCallActivity
import nl.hva.chatstone.api.ServerEvent
import nl.hva.chatstone.api.model.output.ConversationMessage
import nl.hva.chatstone.api.model.output.OutgoingCallOffer
import java.time.Instant

enum class NotificationChannelID {
  Message,
  Call
}

class NotificationEventHandler(
  private val application: ChatstoneApplication,
) {
  private val ctx = application.applicationContext
  private val loader = ImageLoader(ctx)
  private val scope = CoroutineScope(Dispatchers.IO)
  private val sessionVM = application.sessionVM
  private val conversationVM = sessionVM.conversationsVM
  private val manager = application.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

  init {
    scope.launch {
      sessionVM.websocket.start(conversationVM.me.id)
      sessionVM.websocket.websocketEvents.collect {
        onServerEvent(it)
      }
    }
  }

  private fun onServerEvent(event: ServerEvent) {
    Log.v("NotificationEventHandler", "$event")
    when (event) {
      is ServerEvent.MessageCreateEvent -> handleMessageCreate(event.data)
      is ServerEvent.CallOfferEvent -> handleCallOfferEvent(event.data)
      else -> {}
    }
  }

  private fun handleMessageCreate(message: ConversationMessage) {
    if (application.activityIsOpen) return

    val manager = application.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    val channelID = NotificationChannelID.Message.toString()
    val channel = NotificationChannel(channelID, channelID, NotificationManager.IMPORTANCE_HIGH)
    manager.createNotificationChannel(channel)

    val user = conversationVM.conversations.value!!
      .find { it.id == message.conversationID }!!
      .otherParticipant

    val request = ImageRequest.Builder(ctx)
      .data(user.avatarURL)
      .transformations(CircleCropTransformation())
      .build()

    val intent = Intent(application, MainActivity::class.java)
      .setAction("action")

    scope.launch {
      val result = loader.execute(request)
      val bitmap = result.drawable?.toBitmap()
      val icon = Icon.createWithBitmap(bitmap)

      val person = Person.Builder()
        .setName(user.username)
        .setIcon(icon)
        .build()

      val style = Notification.MessagingStyle(person)
        .addMessage(message.content, Instant.parse("${message.createdAt}Z").toEpochMilli(), person)

      val shortcut = ShortcutInfo.Builder(ctx, "shid")
        .setLongLived(true)
        .setIcon(icon)
        .setShortLabel(user.username)
        .setPerson(person)
        .setIntent(intent)
        .build()

      val shortcutManager = ctx.getSystemService(Context.SHORTCUT_SERVICE) as ShortcutManager
      shortcutManager.pushDynamicShortcut(shortcut)

      val notification = Notification.Builder(application, channelID)
        .setStyle(style)
        .setSmallIcon(R.drawable.baseline_message_24)
        .setShortcutId(shortcut.id)
        .build()

      manager.notify(message.id, notification)
    }
  }

  private fun handleCallOfferEvent(callOffer: OutgoingCallOffer) {
    sessionVM.callVM.onCallOffer(callOffer)

    val id = Instant.now().epochSecond.toInt()

    val channelID = NotificationChannelID.Call.toString()
    val channel = NotificationChannel(channelID, channelID, NotificationManager.IMPORTANCE_HIGH)
    manager.createNotificationChannel(channel)

    val user = conversationVM.conversations.value!!
      .find { it.id == callOffer.conversationID }!!
      .otherParticipant

    val request = ImageRequest.Builder(ctx)
      .data(user.avatarURL)
      .transformations(CircleCropTransformation())
      .build()

    scope.launch {
      val result = loader.execute(request)
      val bitmap = result.drawable?.toBitmap()
      val icon = Icon.createWithBitmap(bitmap)

      val person = Person.Builder()
        .setName(user.username)
        .setIcon(icon)
        .build()

      val answerIntent = Intent(application, OngoingCallActivity::class.java)
        .let {
          it.putExtra("notification_id", id)
          PendingIntent.getActivity(ctx, id, it, PendingIntent.FLAG_IMMUTABLE)
        }

      val declineIntent = Intent(application, IncomingCallActivity::class.java) // todo: decline call activity
        .let { PendingIntent.getActivity(ctx, 0, it, PendingIntent.FLAG_IMMUTABLE) }

      val notification = Notification.Builder(application, channelID)
        .setSmallIcon(R.drawable.baseline_message_24)
        .setStyle(Notification.CallStyle.forIncomingCall(person, declineIntent, answerIntent))
        .setFullScreenIntent(answerIntent, true)
        .setCategory(Notification.CATEGORY_CALL)
        .build()

      manager.notify(id, notification)
    }
  }
}