package nl.hva.chatstone.webrtc.peer

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import nl.hva.chatstone.webrtc.utils.addRtcIceCandidate
import nl.hva.chatstone.webrtc.utils.createValue
import nl.hva.chatstone.webrtc.utils.setValue
import kotlinx.coroutines.isActive
import nl.hva.chatstone.webrtc.utils.stringify
import org.webrtc.CandidatePairChangeEvent
import org.webrtc.DataChannel
import org.webrtc.IceCandidate
import org.webrtc.IceCandidateErrorEvent
import org.webrtc.MediaConstraints
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.RTCStatsReport
import org.webrtc.RtpReceiver
import org.webrtc.RtpTransceiver
import org.webrtc.SessionDescription

/**
 * Wrapper around the WebRTC connection that contains tracks.
 *
 * @param coroutineScope The scope used to listen to stats events.
 * @param type The internal type of the PeerConnection. Check [StreamPeerType].
 * @param mediaConstraints Constraints used for the connections.
 * @param onStreamAdded Handler when a new [MediaStream] gets added.
 * @param onNegotiationNeeded Handler when there's a new negotiation.
 * @param onIceCandidate Handler whenever we receive [IceCandidate]s.
 */
class StreamPeerConnection(
  private val coroutineScope: CoroutineScope,
  private val type: StreamPeerType,
  private val mediaConstraints: MediaConstraints,
  private val onStreamAdded: ((MediaStream) -> Unit)?,
  private val onNegotiationNeeded: ((StreamPeerConnection, StreamPeerType) -> Unit)?,
  private val onIceCandidate: ((IceCandidate, StreamPeerType) -> Unit)?,
  private val onVideoTrack: ((RtpTransceiver?) -> Unit)?
) : PeerConnection.Observer {
  private val TAG = "Call:PeerConnection"
  private val typeTag = type.stringify()

  /**
   * The wrapped connection for all the WebRTC communication.
   */
  lateinit var connection: PeerConnection
    private set

  /**
   * Used to manage the stats observation lifecycle.
   */
  private var statsJob: Job? = null

  /**
   * Used to pool together and store [IceCandidate]s before consuming them.
   */
  private val pendingIceMutex = Mutex()
  private val pendingIceCandidates = mutableListOf<IceCandidate>()

  /**
   * Contains stats events for observation.
   */
  private val statsFlow: MutableStateFlow<RTCStatsReport?> = MutableStateFlow(null)

  init {
    Log.v(TAG, "<init> #sfu; #$typeTag; mediaConstraints: $mediaConstraints")
  }

  /**
   * Initialize a [StreamPeerConnection] using a WebRTC [PeerConnection].
   *
   * @param peerConnection The connection that holds audio and video tracks.
   */
  fun initialize(peerConnection: PeerConnection) {
    Log.v(TAG, "[initialize] #sfu; #$typeTag; peerConnection: $peerConnection")
    this.connection = peerConnection
  }

  /**
   * Used to create an offer whenever there's a negotiation that we need to process on the
   * publisher side.
   *
   * @return [Result] wrapper of the [SessionDescription] for the publisher.
   */
  suspend fun createOffer(): Result<SessionDescription> {
    Log.v(TAG, "[createOffer] #sfu; #$typeTag; no args")
    return createValue { connection.createOffer(it, mediaConstraints) }
  }

  /**
   * Used to create an answer whenever there's a subscriber offer.
   *
   * @return [Result] wrapper of the [SessionDescription] for the subscriber.
   */
  suspend fun createAnswer(): Result<SessionDescription> {
    Log.v(TAG, "[createAnswer] #sfu; #$typeTag; no args")
    return createValue { connection.createAnswer(it, mediaConstraints) }
  }

  /**
   * Used to set up the SDP on underlying connections and to add [pendingIceCandidates] to the
   * connection for listening.
   *
   * @param sessionDescription That contains the remote SDP.
   * @return An empty [Result], if the operation has been successful or not.
   */
  suspend fun setRemoteDescription(sessionDescription: SessionDescription): Result<Unit> {
    Log.v(TAG, "[setRemoteDescription] #sfu; #$typeTag; answerSdp: ${sessionDescription.stringify()}")
    return setValue {
      connection.setRemoteDescription(
        it,
        SessionDescription(
          sessionDescription.type,
          sessionDescription.description.mungeCodecs()
        )
      )
    }.also {
      pendingIceMutex.withLock {
        pendingIceCandidates.forEach { iceCandidate ->
          Log.v(TAG, "[setRemoteDescription] #sfu; #subscriber; pendingRtcIceCandidate: $iceCandidate")
          connection.addRtcIceCandidate(iceCandidate)
        }
        pendingIceCandidates.clear()
      }
    }
  }

  /**
   * Sets the local description for a connection either for the subscriber or publisher based on
   * the flow.
   *
   * @param sessionDescription That contains the subscriber or publisher SDP.
   * @return An empty [Result], if the operation has been successful or not.
   */
  suspend fun setLocalDescription(sessionDescription: SessionDescription): Result<Unit> {
    val sdp = SessionDescription(
      sessionDescription.type,
      sessionDescription.description.mungeCodecs()
    )
    Log.v(TAG, "[setLocalDescription] #sfu; #$typeTag; offerSdp: ${sessionDescription.stringify()}")
    return setValue { connection.setLocalDescription(it, sdp) }
  }

  /**
   * Adds an [IceCandidate] to the underlying [connection] if it's already been set up, or stores
   * it for later consumption.
   *
   * @param iceCandidate To process and add to the connection.
   * @return An empty [Result], if the operation has been successful or not.
   */
  suspend fun addIceCandidate(iceCandidate: IceCandidate): Result<Unit> {
    if (connection.remoteDescription == null) {
      Log.w(TAG, "[addIceCandidate] #sfu; #$typeTag; postponed (no remoteDescription): $iceCandidate")
      pendingIceMutex.withLock {
        pendingIceCandidates.add(iceCandidate)
      }
      return Result.failure(RuntimeException("RemoteDescription is not set"))
    }
    Log.v(TAG, "[addIceCandidate] #sfu; #$typeTag; rtcIceCandidate: $iceCandidate")
    return connection.addRtcIceCandidate(iceCandidate).also {
      Log.v(TAG, "[addIceCandidate] #sfu; #$typeTag; completed: $it")
    }
  }

  /**
   * Peer connection listeners.
   */

  /**
   * Triggered whenever there's a new [RtcIceCandidate] for the call. Used to update our tracks
   * and subscriptions.
   *
   * @param candidate The new candidate.
   */
  override fun onIceCandidate(candidate: IceCandidate?) {
    Log.v(TAG, "[onIceCandidate] #sfu; #$typeTag; candidate: $candidate")
    if (candidate == null) return

    onIceCandidate?.invoke(candidate, type)
  }

  /**
   * Triggered whenever there's a new [MediaStream] that was added to the connection.
   *
   * @param stream The stream that contains audio or video.
   */
  override fun onAddStream(stream: MediaStream?) {
    Log.v(TAG, "[onAddStream] #sfu; #$typeTag; stream: $stream")
    if (stream != null) {
      onStreamAdded?.invoke(stream)
    }
  }

  /**
   * Triggered whenever there's a new [MediaStream] or [MediaStreamTrack] that's been added
   * to the call. It contains all audio and video tracks for a given session.
   *
   * @param receiver The receiver of tracks.
   * @param mediaStreams The streams that were added containing their appropriate tracks.
   */
  override fun onAddTrack(receiver: RtpReceiver?, mediaStreams: Array<out MediaStream>?) {
    Log.v(TAG, "[onAddTrack] #sfu; #$typeTag; receiver: $receiver, mediaStreams: $mediaStreams")
    mediaStreams?.forEach { mediaStream ->
      Log.v(TAG, "[onAddTrack] #sfu; #$typeTag; mediaStream: $mediaStream")
      mediaStream.audioTracks?.forEach { remoteAudioTrack ->
        Log.v(TAG, "[onAddTrack] #sfu; #$typeTag; remoteAudioTrack: ${remoteAudioTrack.stringify()}")
        remoteAudioTrack.setEnabled(true)
      }
      onStreamAdded?.invoke(mediaStream)
    }
  }

  /**
   * Triggered whenever there's a new negotiation needed for the active [PeerConnection].
   */
  override fun onRenegotiationNeeded() {
    Log.v(TAG, "[onRenegotiationNeeded] #sfu; #$typeTag; no args")
    onNegotiationNeeded?.invoke(this, type)
  }

  /**
   * Triggered whenever a [MediaStream] was removed.
   *
   * @param stream The stream that was removed from the connection.
   */
  override fun onRemoveStream(stream: MediaStream?) {}

  /**
   * Triggered when the connection state changes.  Used to start and stop the stats observing.
   *
   * @param newState The new state of the [PeerConnection].
   */
  override fun onIceConnectionChange(newState: PeerConnection.IceConnectionState) {
    Log.v(TAG, "[onIceConnectionChange] #sfu; #$typeTag; newState: $newState")
    when (newState) {
      PeerConnection.IceConnectionState.CLOSED,
      PeerConnection.IceConnectionState.FAILED,
      PeerConnection.IceConnectionState.DISCONNECTED -> statsJob?.cancel()
      PeerConnection.IceConnectionState.CONNECTED -> statsJob = observeStats()
      else -> Unit
    }
  }

  /**
   * @return The [RTCStatsReport] for the active connection.
   */
  fun getStats(): StateFlow<RTCStatsReport?> {
    return statsFlow
  }

  /**
   * Observes the local connection stats and emits it to [statsFlow] that users can consume.
   */
  private fun observeStats() = coroutineScope.launch {
    while (isActive) {
      delay(10_000L)
      connection.getStats {
        Log.v(TAG, "[observeStats] #sfu; #$typeTag; stats: $it")
        statsFlow.value = it
      }
    }
  }

  override fun onTrack(transceiver: RtpTransceiver?) {
    Log.v(TAG, "[onTrack] #sfu; #$typeTag; transceiver: $transceiver")
    onVideoTrack?.invoke(transceiver)
  }

  /**
   * Domain - [PeerConnection] and [PeerConnection.Observer] related callbacks.
   */
  override fun onRemoveTrack(receiver: RtpReceiver?) {
    Log.v(TAG, "[onRemoveTrack] #sfu; #$typeTag; receiver: $receiver")
  }

  override fun onSignalingChange(newState: PeerConnection.SignalingState?) {
    Log.v(TAG, "[onSignalingChange] #sfu; #$typeTag; newState: $newState")
  }

  override fun onIceConnectionReceivingChange(receiving: Boolean) {
    Log.v(TAG, "[onIceConnectionReceivingChange] #sfu; #$typeTag; receiving: $receiving")
  }

  override fun onIceGatheringChange(newState: PeerConnection.IceGatheringState?) {
    Log.v(TAG, "[onIceGatheringChange] #sfu; #$typeTag; newState: $newState")
  }

  override fun onIceCandidatesRemoved(iceCandidates: Array<out org.webrtc.IceCandidate>?) {
    Log.v(TAG, "[onIceCandidatesRemoved] #sfu; #$typeTag; iceCandidates: $iceCandidates")
  }

  override fun onIceCandidateError(event: IceCandidateErrorEvent?) {
    Log.e(TAG, "[onIceCandidateError] #sfu; #$typeTag; event: ${event?.stringify()}")
  }

  override fun onConnectionChange(newState: PeerConnection.PeerConnectionState) {
    Log.v(TAG, "[onConnectionChange] #sfu; #$typeTag; newState: $newState")
  }

  override fun onSelectedCandidatePairChanged(event: CandidatePairChangeEvent?) {
    Log.v(TAG, "[onSelectedCandidatePairChanged] #sfu; #$typeTag; event: $event")
  }

  override fun onDataChannel(channel: DataChannel?): Unit = Unit

  override fun toString(): String =
    "StreamPeerConnection(type='$typeTag', constraints=$mediaConstraints)"

  private fun String.mungeCodecs(): String {
    return this.replace("vp9", "VP9").replace("vp8", "VP8").replace("h264", "H264")
  }
}
