package nl.hva.chatstone

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle
import nl.hva.chatstone.activities.MainActivity
import nl.hva.chatstone.activities.OngoingCallActivity
import nl.hva.chatstone.service.ChatstoneService
import nl.hva.chatstone.viewmodel.SessionViewModel
import nl.hva.chatstone.webrtc.SignalingClient
import nl.hva.chatstone.webrtc.WebRtcSessionManager
import nl.hva.chatstone.webrtc.peer.StreamPeerConnectionFactory

class ChatstoneApplication : Application(), Application.ActivityLifecycleCallbacks {
  var activityIsOpen = false
  var mainActivity: MainActivity? = null
  var callActivity: OngoingCallActivity? = null
  val sessionVM by lazy { SessionViewModel(this) }
  val signalingClient by lazy { SignalingClient(this) }
  val connectionFactory by lazy { StreamPeerConnectionFactory(this) }

  private var _webRtcSessionManager: WebRtcSessionManager? = null
  val webRtcSessionManager: WebRtcSessionManager
    get() {
      synchronized(this) {
        if (_webRtcSessionManager != null) {
          return _webRtcSessionManager!!
        }

        _webRtcSessionManager = WebRtcSessionManager(
          this,
          signalingClient,
          connectionFactory,
        )
        return _webRtcSessionManager!!
      }
    }

  fun deleteWebRTCManager() {
    _webRtcSessionManager = null
  }

  override fun onCreate() {
    super.onCreate()
    registerActivityLifecycleCallbacks(this)
    startChatstoneService()
  }

  fun startChatstoneService() {
    startService(getChatstoneServiceIntent())
  }

  fun stopChatstoneService() {
    stopService(getChatstoneServiceIntent())
  }

  private fun getChatstoneServiceIntent() =
    Intent(this, ChatstoneService::class.java)

  override fun onActivityStarted(activity: Activity) {
    if (activity is OngoingCallActivity) callActivity = activity

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