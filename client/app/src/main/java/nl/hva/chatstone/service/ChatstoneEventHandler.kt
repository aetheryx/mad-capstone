package nl.hva.chatstone.service

import android.app.Activity
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import nl.hva.chatstone.ChatstoneApplication
import nl.hva.chatstone.R
import nl.hva.chatstone.activities.DeclineCallActivity
import nl.hva.chatstone.activities.MainActivity
import nl.hva.chatstone.activities.OngoingCallActivity
import nl.hva.chatstone.activities.ToURLActivity
import nl.hva.chatstone.api.ServerEvent
import nl.hva.chatstone.api.model.output.ConversationMessage
import nl.hva.chatstone.api.model.output.OutgoingCallOffer
import androidx.core.content.getSystemService


class ChatstoneEventHandler(
  private val application: ChatstoneApplication,
) {
  private val TAG = "ChatstoneEventHandler"
  private val ctx = application.applicationContext
  private val scope = CoroutineScope(Dispatchers.IO)
  private val sessionVM = application.sessionVM
  private val conversationVM = sessionVM.conversationsVM
  private val notificationChannelManager = NotificationChannelManager(application)
  private val notificationManager = ctx.getSystemService<NotificationManager>()!!
  private val shortcutManager = ctx.getSystemService<ShortcutManager>()!!

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
      is ServerEvent.CallHangUpEvent -> handleCallHangupEvent(event.data)
      else -> {}
    }
  }

  private suspend fun handleMessageCreate(message: ConversationMessage) {
    if (application.activityIsOpen) return

    val channel = notificationChannelManager.createMessagesChannel()

    val conversation = conversationVM.getConversation(message.conversationID)!!
    val person = conversation.otherParticipant.toPerson(ctx)

    val shortcutIntent = Intent(application, MainActivity::class.java)
      .setAction("action")

    val notificationIntent = Intent(application, ToURLActivity::class.java)
      .let {
        it.putExtra("target_url", "/conversations/${message.conversationID}")
        PendingIntent.getActivity(ctx, message.id, it, PendingIntent.FLAG_IMMUTABLE)
      }

    val style = Notification.MessagingStyle(person)
      .addMessage(message.content, message.createdAt.toEpochSecond() * 1000L, person)

    val shortcut = ShortcutInfo.Builder(ctx, "chatstone_to_message")
      .setLongLived(true)
      .setIcon(person.icon)
      .setShortLabel(person.name!!)
      .setPerson(person)
      .setIntent(shortcutIntent)
      .build()
      .also {
        shortcutManager.pushDynamicShortcut(it)
      }

    val notification = Notification.Builder(application, channel.id)
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

    val channel = notificationChannelManager.createCallChannel()

    val answerIntent = createNotificationIntent(OngoingCallActivity::class.java, callOffer.callID)
    val declineIntent = createNotificationIntent(DeclineCallActivity::class.java, callOffer.callID)

    val conversation = conversationVM.getConversation(callOffer.conversationID)!!
    val person = conversation.otherParticipant.toPerson(ctx)

    val notification = Notification.Builder(application, channel.id)
      .setSmallIcon(R.drawable.hva)
      .setStyle(Notification.CallStyle.forIncomingCall(person, declineIntent, answerIntent))
      .setFullScreenIntent(answerIntent, true)
      .setFlag(Notification.FLAG_INSISTENT, true)
      .setCategory(Notification.CATEGORY_CALL)
      .build()

    notificationManager.notify(callOffer.callID, notification)
  }

  private fun <T : Activity>createNotificationIntent(activity: Class<T>, id: Int): PendingIntent {
    val intent = Intent(application, activity)
    intent.putExtra("notification_id", id)
    return PendingIntent.getActivity(ctx, id, intent, PendingIntent.FLAG_IMMUTABLE)
  }

  private fun handleCallHangupEvent(callID: Int) {
    notificationManager.cancel(callID)
  }
}