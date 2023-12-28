package nl.hva.capstone.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Person
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import nl.hva.capstone.CapstoneApplication
import nl.hva.capstone.R
import nl.hva.capstone.activities.CallActivity
import nl.hva.capstone.api.ServerEvent
import nl.hva.capstone.api.model.input.IncomingCallOffer
import nl.hva.capstone.api.model.output.ConversationMessage
import nl.hva.capstone.api.model.output.OutgoingCallOffer
import java.time.Instant
import java.util.Date

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

    val title = conversationVM.conversations.value!!
      .find { it.id == message.conversationID }!!
      .otherParticipant.username

    val notification = Notification.Builder(application, channelID)
        .setContentTitle(title)
        .setContentText(message.content)
        .setSmallIcon(R.drawable.default_pfp)
        .build()

    manager.notify(message.id, notification)
  }

  private fun handleCallOfferEvent(callOffer: OutgoingCallOffer) {
    val manager = application.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    val channelID = NotificationChannelID.Call.toString()
    val channel = NotificationChannel(channelID, channelID, NotificationManager.IMPORTANCE_HIGH)
    manager.createNotificationChannel(channel)

    val person = Person.Builder()
      .setName("bob")
      .build()

    val callIntent = Intent(application, CallActivity::class.java)

    val answerIntent = PendingIntent.getActivity(application.applicationContext, 0, callIntent,
      PendingIntent.FLAG_IMMUTABLE)

    val declineIntent = PendingIntent.getActivity(application.applicationContext, 0, callIntent,
      PendingIntent.FLAG_IMMUTABLE)

    val notification = Notification.Builder(application, channelID)
      .setSmallIcon(R.drawable.default_pfp)
      .setStyle(Notification.CallStyle.forIncomingCall(person, answerIntent, declineIntent))
      .setFullScreenIntent(answerIntent, true)
      .build()

    manager.notify(Instant.now().epochSecond.toInt(), notification)
  }
}