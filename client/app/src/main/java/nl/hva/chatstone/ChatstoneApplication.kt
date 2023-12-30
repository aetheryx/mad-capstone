package nl.hva.chatstone

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle
import nl.hva.chatstone.activities.MainActivity
import nl.hva.chatstone.service.ChatstoneService
import nl.hva.chatstone.viewmodel.SessionViewModel
import nl.hva.chatstone.webrtc.SignalingClient

class ChatstoneApplication : Application(), Application.ActivityLifecycleCallbacks {
  var activityIsOpen = false
  var mainActivity: MainActivity? = null
  val sessionVM by lazy { SessionViewModel(this) }
  val signalingClient by lazy { SignalingClient(this) }

  override fun onCreate() {
    super.onCreate()
    registerActivityLifecycleCallbacks(this)

    val intent = Intent(this, ChatstoneService::class.java)
    startService(intent)
  }

  override fun onActivityStarted(activity: Activity) {
    if (activity !is MainActivity) return
    activityIsOpen = true
    mainActivity = activity
  }

  override fun onActivityResumed(activity: Activity) {
    if (activity !is MainActivity) return
    activityIsOpen = true
  }

  override fun onActivityPaused(activity: Activity) {
    if (activity !is MainActivity) return
    activityIsOpen = false
  }

  override fun onActivityStopped(activity: Activity) {
    if (activity !is MainActivity) return
    activityIsOpen = false
  }

  override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
  override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
  override fun onActivityDestroyed(activity: Activity) {}
}