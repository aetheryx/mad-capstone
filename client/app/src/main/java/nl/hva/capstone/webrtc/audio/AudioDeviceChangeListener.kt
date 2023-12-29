package nl.hva.capstone.webrtc.audio

typealias AudioDeviceChangeListener = (
  audioDevices: List<AudioDevice>,
  selectedAudioDevice: AudioDevice?
) -> Unit
