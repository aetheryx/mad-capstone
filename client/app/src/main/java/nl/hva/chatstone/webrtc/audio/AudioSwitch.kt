package nl.hva.chatstone.webrtc.audio

import android.content.Context
import android.media.AudioManager
import android.util.Log

class AudioSwitch internal constructor(
  context: Context,
  audioFocusChangeListener: AudioManager.OnAudioFocusChangeListener,
  preferredDeviceList: List<Class<out AudioDevice>>,
  private val audioManager: AudioManagerAdapter = AudioManagerAdapterImpl(
    context,
    context.getSystemService(Context.AUDIO_SERVICE) as AudioManager,
    audioFocusChangeListener = audioFocusChangeListener
  )
) {
  private val TAG = "Call:AudioSwitch"
  private var audioDeviceChangeListener: AudioDeviceChangeListener? = null
  private var selectedDevice: AudioDevice? = null
  private var userSelectedDevice: AudioDevice? = null
  private var wiredHeadsetAvailable = false
  private val mutableAudioDevices = ArrayList<AudioDevice>()
  private val preferredDeviceList: List<Class<out AudioDevice>>

  private var state: State = State.STOPPED

  internal enum class State {
    STARTED, ACTIVATED, STOPPED
  }

  init {
    this.preferredDeviceList = getPreferredDeviceList(preferredDeviceList)
  }

  private fun getPreferredDeviceList(preferredDeviceList: List<Class<out AudioDevice>>):
          List<Class<out AudioDevice>> {
    require(hasNoDuplicates(preferredDeviceList))

    return if (preferredDeviceList.isEmpty() || preferredDeviceList == defaultPreferredDeviceList) {
      defaultPreferredDeviceList
    } else {
      val result = defaultPreferredDeviceList.toMutableList()
      result.removeAll(preferredDeviceList)
      preferredDeviceList.forEachIndexed { index, device ->
        result.add(index, device)
      }
      result
    }
  }

  /**
   * Starts listening for audio device changes and calls the [listener] upon each change.
   * **Note:** When audio device listening is no longer needed, [AudioSwitch.stop] should be
   * called in order to prevent a memory leak.
   */
  fun start(listener: AudioDeviceChangeListener) {
    audioDeviceChangeListener = listener
    if (state == State.STOPPED) {
      enumerateDevices()
      state = State.STARTED
    }
  }

  /**
   * Stops listening for audio device changes if [AudioSwitch.start] has already been
   * invoked. [AudioSwitch.deactivate] will also get called if a device has been activated
   * with [AudioSwitch.activate].
   */
  fun stop() {
    when (state) {
      State.ACTIVATED -> {
        deactivate()
        closeListeners()
      }

      State.STARTED -> {
        closeListeners()
      }

      State.STOPPED -> {}
    }
  }

  /**
   * Performs audio routing and unmuting on the selected device from
   * [AudioSwitch.selectDevice]. Audio focus is also acquired for the client application.
   * **Note:** [AudioSwitch.deactivate] should be invoked to restore the prior audio
   * state.
   */
  fun activate() {
    when (state) {
      State.STARTED -> {
        audioManager.cacheAudioState()

        // Always set mute to false for WebRTC
        audioManager.mute(false)
        audioManager.setAudioFocus()
        selectedDevice?.let { activate(it) }
        state = State.ACTIVATED
      }

      State.ACTIVATED -> selectedDevice?.let { activate(it) }
      State.STOPPED -> throw IllegalStateException()
    }
  }

  /**
   * Restores the audio state prior to calling [AudioSwitch.activate] and removes
   * audio focus from the client application.
   */
  private fun deactivate() {
    when (state) {
      State.ACTIVATED -> {
        // Restore stored audio state
        audioManager.restoreAudioState()
        state = State.STARTED
      }

      State.STARTED, State.STOPPED -> {}
    }
  }

  /**
   * Selects the desired [audioDevice]. If the provided [AudioDevice] is not
   * available, no changes are made. If the provided device is null, one is chosen based on the
   * specified preferred device list or the following default list:
   * [BluetoothHeadset], [WiredHeadset], [Earpiece], [Speakerphone].
   */
  private fun selectDevice(audioDevice: AudioDevice?) {
    Log.v(TAG, "[selectDevice] audioDevice: $audioDevice")
    if (selectedDevice != audioDevice) {
      userSelectedDevice = audioDevice
      enumerateDevices()
    }
  }

  private fun hasNoDuplicates(list: List<Class<out AudioDevice>>) =
    list.groupingBy { it }.eachCount().filter { it.value > 1 }.isEmpty()

  private fun activate(audioDevice: AudioDevice) {
    when (audioDevice) {
      is AudioDevice.BluetoothHeadset -> audioManager.enableSpeakerphone(false)
      is AudioDevice.Earpiece, is AudioDevice.WiredHeadset -> audioManager.enableSpeakerphone(false)
      is AudioDevice.Speakerphone -> audioManager.enableSpeakerphone(true)
    }
  }

  internal data class AudioDeviceState(
    val audioDeviceList: List<AudioDevice>,
    val selectedAudioDevice: AudioDevice?
  )

  private fun enumerateDevices(bluetoothHeadsetName: String? = null) {
    Log.v(TAG, "[enumerateDevices] bluetoothHeadsetName: $bluetoothHeadsetName")
    // save off the old state and 'semi'-deep copy the list of audio devices
    val oldAudioDeviceState = AudioDeviceState(mutableAudioDevices.map { it }, selectedDevice)
    // update audio device list and selected device
    addAvailableAudioDevices(bluetoothHeadsetName)

    if (!userSelectedDevicePresent(mutableAudioDevices)) {
      userSelectedDevice = null
    }

    // Select the audio device
    selectedDevice = if (userSelectedDevice != null) {
      userSelectedDevice
    } else if (mutableAudioDevices.size > 0) {
      mutableAudioDevices.first()
    } else {
      null
    }

    Log.v(TAG, "[enumerateDevices] selectedDevice: $selectedDevice")

    // Activate the device if in the active state
    if (state == State.ACTIVATED) {
      activate()
    }
    // trigger audio device change listener if there has been a change
    val newAudioDeviceState = AudioDeviceState(mutableAudioDevices, selectedDevice)
    if (newAudioDeviceState != oldAudioDeviceState) {
      audioDeviceChangeListener?.invoke(mutableAudioDevices, selectedDevice)
    }
  }

  private fun addAvailableAudioDevices(bluetoothHeadsetName: String?) {
    Log.v(
      TAG, "[addAvailableAudioDevices] wiredHeadsetAvailable: $wiredHeadsetAvailable, " +
              "bluetoothHeadsetName: $bluetoothHeadsetName"
    )
    mutableAudioDevices.clear()
    preferredDeviceList.forEach { audioDevice ->
      Log.v(TAG, "[addAvailableAudioDevices] audioDevice: ${audioDevice.simpleName}")
      when (audioDevice) {
        AudioDevice.BluetoothHeadset::class.java -> {
          /*
           * Since the there is a delay between receiving the ACTION_ACL_CONNECTED event and receiving
           * the name of the connected device from querying the BluetoothHeadset proxy class, the
           * headset name received from the ACTION_ACL_CONNECTED intent needs to be passed into this
           * function.
           */
        }

        AudioDevice.WiredHeadset::class.java -> {
          Log.v(
            TAG,
            "[addAvailableAudioDevices] #WiredHeadset; wiredHeadsetAvailable: $wiredHeadsetAvailable"
          )
          if (wiredHeadsetAvailable) {
            mutableAudioDevices.add(AudioDevice.WiredHeadset())
          }
        }

        AudioDevice.Earpiece::class.java -> {
          val hasEarpiece = audioManager.hasEarpiece()
          Log.v(
            TAG, "[addAvailableAudioDevices] #Earpiece; hasEarpiece: $hasEarpiece, " +
                    "wiredHeadsetAvailable: $wiredHeadsetAvailable"
          )
          if (hasEarpiece && !wiredHeadsetAvailable) {
            mutableAudioDevices.add(AudioDevice.Earpiece())
          }
        }

        AudioDevice.Speakerphone::class.java -> {
          val hasSpeakerphone = audioManager.hasSpeakerphone()
          Log.v(TAG, "[addAvailableAudioDevices] #Speakerphone; hasSpeakerphone: $hasSpeakerphone")
          if (hasSpeakerphone) {
            mutableAudioDevices.add(AudioDevice.Speakerphone())
          }
        }
      }
    }
  }

  private fun userSelectedDevicePresent(audioDevices: List<AudioDevice>) =
    userSelectedDevice?.let { selectedDevice ->
      if (selectedDevice is AudioDevice.BluetoothHeadset) {
        // Match any bluetooth headset as a new one may have been connected
        audioDevices.find { it is AudioDevice.BluetoothHeadset }?.let { newHeadset ->
          userSelectedDevice = newHeadset
          true
        } ?: false
      } else {
        audioDevices.contains(selectedDevice)
      }
    } ?: false

  private fun closeListeners() {
    audioDeviceChangeListener = null
    state = State.STOPPED
  }

  companion object {
    private val defaultPreferredDeviceList by lazy {
      listOf(
        AudioDevice.BluetoothHeadset::class.java,
        AudioDevice.WiredHeadset::class.java,
        AudioDevice.Earpiece::class.java,
        AudioDevice.Speakerphone::class.java
      )
    }
  }
}
