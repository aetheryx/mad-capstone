package nl.hva.capstone.service

import android.util.Log
import nl.hva.capstone.CapstoneApplication
import nl.hva.capstone.api.CapstoneWebsocket
import nl.hva.capstone.api.ServerEvent

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

    Log.v(TAG, "got: $event")
  }
}