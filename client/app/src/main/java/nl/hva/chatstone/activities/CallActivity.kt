package nl.hva.chatstone.activities

import android.app.NotificationManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.core.content.getSystemService
import nl.hva.chatstone.ChatstoneApplication

open class CallActivity : ComponentActivity() {
  protected val application by lazy {
    getApplication() as ChatstoneApplication
  }
  private val notificationService by lazy {
    applicationContext.getSystemService<NotificationManager>()!!
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    intent.extras?.getInt("notification_id")?.let {
      notificationService.cancel(it)
    }
  }
}