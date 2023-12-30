package nl.hva.chatstone.webrtc.audio

typealias AudioDeviceChangeListener = (
  audioDevices: List<AudioDevice>,
  selectedAudioDevice: AudioDevice?
) -> Unit
