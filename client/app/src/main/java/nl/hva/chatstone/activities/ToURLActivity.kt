package nl.hva.chatstone.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import nl.hva.chatstone.ChatstoneApplication

class ToURLActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val sessionVM = (application as ChatstoneApplication).sessionVM
    sessionVM.targetURL.value = intent.extras!!.getString("target_url")

    val intent = Intent(this, MainActivity::class.java)
    startActivity(intent)
  }
}