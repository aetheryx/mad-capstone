package nl.hva.chatstone.webrtc.audio

import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioDeviceInfo
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.util.Log

internal class AudioManagerAdapterImpl(
  private val context: Context,
  private val audioManager: AudioManager,
  private val audioFocusRequest: AudioFocusRequestWrapper = AudioFocusRequestWrapper(),
  private val audioFocusChangeListener: AudioManager.OnAudioFocusChangeListener
) : AudioManagerAdapter {
  private val TAG = "Call:AudioManagerAdapter"
  private var savedAudioMode = 0
  private var savedIsMicrophoneMuted = false
  private var savedSpeakerphoneEnabled = false
  private var audioRequest: AudioFocusRequest? = null

  override fun hasEarpiece(): Boolean {
    return context.packageManager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY)
  }

  override fun hasSpeakerphone(): Boolean {
    return if (context.packageManager.hasSystemFeature(PackageManager.FEATURE_AUDIO_OUTPUT)) {
      val devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
      for (device in devices) {
        if (device.type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER) {
          return true
        }
      }
      false
    } else {
      true
    }
  }

  override fun setAudioFocus() {
    // Request audio focus before making any device switch.
    audioRequest = audioFocusRequest.buildRequest(audioFocusChangeListener)
    audioRequest?.let {
      val result = audioManager.requestAudioFocus(it)
      Log.v(TAG, "[setAudioFocus] $result")
    }
    /*
     * Start by setting MODE_IN_COMMUNICATION as default audio mode. It is
     * required to be in this mode when playout and/or recording starts for
     * best possible VoIP performance. Some devices have difficulties with speaker mode
     * if this is not set.
     */
    audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
  }

  override fun enableBluetoothSco(enable: Boolean) {
    Log.v(TAG, "[enableBluetoothSco] enable: $enable")
    audioManager.run { if (enable) startBluetoothSco() else stopBluetoothSco() }
  }

  override fun enableSpeakerphone(enable: Boolean) {
    Log.v(TAG, "[enableSpeakerphone] enable: $enable")
    audioManager.isSpeakerphoneOn = enable
  }

  override fun mute(mute: Boolean) {
    Log.v(TAG, "[mute] mute: $mute")
    audioManager.isMicrophoneMute = mute
  }

  // TODO Consider persisting audio state in the event of process death
  override fun cacheAudioState() {
    Log.v(TAG, "[cacheAudioState]")
    savedAudioMode = audioManager.mode
    savedIsMicrophoneMuted = audioManager.isMicrophoneMute
    savedSpeakerphoneEnabled = audioManager.isSpeakerphoneOn
  }

  override fun restoreAudioState() {
    Log.v(TAG, "[restoreAudioState]")
    audioManager.mode = savedAudioMode
    mute(savedIsMicrophoneMuted)
    enableSpeakerphone(savedSpeakerphoneEnabled)
    audioRequest?.let {
      val result = audioManager.abandonAudioFocusRequest(it)
      Log.v(TAG, "[abandonAudioFocusRequest]: $it $result")
    }
  }
}
