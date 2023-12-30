package nl.hva.chatstone.webrtc.audio

import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager

internal class AudioFocusRequestWrapper {
  fun buildRequest(audioFocusChangeListener: AudioManager.OnAudioFocusChangeListener): AudioFocusRequest {
    val playbackAttributes = AudioAttributes.Builder()
      .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
      .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
      .build()

    return AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
      .setAudioAttributes(playbackAttributes)
      .setAcceptsDelayedFocusGain(true)
      .setOnAudioFocusChangeListener(audioFocusChangeListener)
      .build()
  }
}
