package nl.hva.capstone

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import nl.hva.capstone.viewmodel.SessionViewModel


class CapstoneApplication : Application(), Application.ActivityLifecycleCallbacks {
  var activityIsOpen = false
  val sessionVM by lazy { SessionViewModel(this) }

  override fun onCreate() {
    super.onCreate()
    registerActivityLifecycleCallbacks(this)
  }

  override fun onActivityStarted(activity: Activity) {
    activityIsOpen = true
  }

  override fun onActivityResumed(activity: Activity) {
    activityIsOpen = true
  }

  override fun onActivityPaused(activity: Activity) {
    activityIsOpen = false
  }

  override fun onActivityStopped(activity: Activity) {
    activityIsOpen = false
  }

  override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
  override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
  override fun onActivityDestroyed(activity: Activity) {}
}