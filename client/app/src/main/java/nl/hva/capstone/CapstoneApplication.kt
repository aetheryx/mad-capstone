package nl.hva.capstone

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle
import nl.hva.capstone.activities.MainActivity
import nl.hva.capstone.service.NotificationService
import nl.hva.capstone.viewmodel.SessionViewModel

class CapstoneApplication : Application(), Application.ActivityLifecycleCallbacks {
  var activityIsOpen = false
  var mainActivity: MainActivity? = null
  val sessionVM by lazy { SessionViewModel(this) }

  override fun onCreate() {
    super.onCreate()
    registerActivityLifecycleCallbacks(this)

    val intent = Intent(this, NotificationService::class.java)
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