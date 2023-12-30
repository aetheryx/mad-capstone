package nl.hva.chatstone.webrtc.sessions

import kotlinx.coroutines.flow.SharedFlow
import nl.hva.chatstone.webrtc.SignalingClient
import nl.hva.chatstone.webrtc.peer.StreamPeerConnectionFactory
import org.webrtc.VideoTrack

interface WebRtcSessionManager {

  val signalingClient: SignalingClient

  val peerConnectionFactory: StreamPeerConnectionFactory

  val localVideoTrackFlow: SharedFlow<VideoTrack>

  val remoteVideoTrackFlow: SharedFlow<VideoTrack>

  fun onSessionScreenReady()

  fun flipCamera()

  fun enableMicrophone(enabled: Boolean)

  fun enableCamera(enabled: Boolean)

  fun disconnect()
}
