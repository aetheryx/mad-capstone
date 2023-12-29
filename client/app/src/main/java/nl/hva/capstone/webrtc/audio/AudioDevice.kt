package nl.hva.capstone.webrtc.audio

sealed class AudioDevice {

  /** The friendly name of the device.*/
  abstract val name: String

  data class BluetoothHeadset internal constructor(override val name: String = "Bluetooth") :
    AudioDevice()

  data class WiredHeadset internal constructor(override val name: String = "Wired Headset") :
    AudioDevice()

  data class Earpiece internal constructor(override val name: String = "Earpiece") : AudioDevice()

  data class Speakerphone internal constructor(override val name: String = "Speakerphone") :
    AudioDevice()
}
