package nl.hva.chatstone.activities

import android.os.Bundle

class DeclineCallActivity : CallActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val callVM = application.sessionVM.callVM
    callVM.declineCall()

    finish()
  }
}