package nl.hva.chatstone.activities

import android.app.NotificationManager
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import nl.hva.chatstone.ChatstoneApplication

open class CallActivity() : ComponentActivity() {
  protected val application by lazy {
    getApplication() as ChatstoneApplication
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    intent.extras?.getInt("notification_id")?.let {
      val manager = application.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
      manager.cancel(it)
    }
  }
}