package nl.hva.chatstone.webrtc

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CameraMetadata
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Handler
import android.os.HandlerThread
import android.os.Process
import android.util.Log
import androidx.core.content.getSystemService
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.android.asCoroutineDispatcher
import kotlinx.coroutines.launch
import nl.hva.chatstone.ChatstoneApplication
import nl.hva.chatstone.webrtc.audio.AudioSwitchHandler
import nl.hva.chatstone.webrtc.peer.StreamPeerConnection
import nl.hva.chatstone.webrtc.peer.StreamPeerConnectionFactory
import nl.hva.chatstone.webrtc.peer.StreamPeerType
import nl.hva.chatstone.webrtc.utils.stringify
import org.webrtc.AudioTrack
import org.webrtc.Camera2Capturer
import org.webrtc.Camera2Enumerator
import org.webrtc.CameraEnumerationAndroid
import org.webrtc.IceCandidate
import org.webrtc.MediaConstraints
import org.webrtc.MediaStreamTrack
import org.webrtc.SessionDescription
import org.webrtc.SurfaceTextureHelper
import org.webrtc.VideoCapturer
import org.webrtc.VideoTrack
import java.util.UUID


private const val ICE_SEPARATOR = '$'

class WebRtcSessionManager(
  private val context: Context,
  val signalingClient: SignalingClient,
  val peerConnectionFactory: StreamPeerConnectionFactory
) {
  private val TAG = "WebRTCSessionManager"

  private val handlerThread by lazy {
    val handlerThread = HandlerThread("webrtc_t", Process.THREAD_PRIORITY_VIDEO)
    handlerThread.start()
    handlerThread
  }
  private val sessionManagerScope by lazy {
    val handler = Handler(handlerThread.looper)
    CoroutineScope(handler.asCoroutineDispatcher())
  }

  val localVideoTrackData = MutableLiveData<VideoTrack>()
  val remoteVideoTrackData = MutableLiveData<VideoTrack>()

  // declaring video constraints and setting OfferToReceiveVideo to true
  // this step is mandatory to create valid offer and answer
  private val mediaConstraints = MediaConstraints().apply {
    mandatory.addAll(
      listOf(
        MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"),
        MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true")
      )
    )
  }

  // getting front camera
  private val videoCapturer: VideoCapturer by lazy { buildCameraCapturer() }
  private val cameraManager by lazy { context.getSystemService<CameraManager>() }
  private val cameraEnumerator: Camera2Enumerator by lazy {
    Camera2Enumerator(context)
  }

  private val resolution: CameraEnumerationAndroid.CaptureFormat
    get() {
      val frontCamera = cameraEnumerator.deviceNames.first { cameraName ->
        cameraEnumerator.isFrontFacing(cameraName)
      }
      val supportedFormats = cameraEnumerator.getSupportedFormats(frontCamera) ?: emptyList()
      return supportedFormats.firstOrNull {
        (it.width == 720 || it.width == 480 || it.width == 360)
      } ?: error("There is no matched resolution!")
    }

  // we need it to initialize video capturer
  private val surfaceTextureHelper = SurfaceTextureHelper.create(
    "SurfaceTextureHelperThread",
    peerConnectionFactory.eglBaseContext
  )

  private val videoSource by lazy {
    peerConnectionFactory.makeVideoSource(videoCapturer.isScreencast).apply {
      videoCapturer.initialize(surfaceTextureHelper, context, this.capturerObserver)
      videoCapturer.startCapture(resolution.width, resolution.height, 30)
    }
  }

  private val localVideoTrack: VideoTrack by lazy {
    peerConnectionFactory.makeVideoTrack(
      source = videoSource,
      trackId = "Video${UUID.randomUUID()}"
    )
  }

  /** Audio properties */

  private val audioHandler by lazy {
    AudioSwitchHandler(context)
  }

  private val audioManager by lazy {
    context.getSystemService<AudioManager>()
  }

  private val audioConstraints: MediaConstraints by lazy {
    buildAudioConstraints()
  }

  private val audioSource by lazy {
    peerConnectionFactory.makeAudioSource(audioConstraints)
  }

  private val localAudioTrack: AudioTrack by lazy {
    peerConnectionFactory.makeAudioTrack(
      source = audioSource,
      trackId = "Audio${UUID.randomUUID()}"
    )
  }

  private var offer: String? = null

  private val peerConnection: StreamPeerConnection by lazy {
    peerConnectionFactory.makePeerConnection(
      coroutineScope = sessionManagerScope,
      configuration = peerConnectionFactory.rtcConfig,
      type = StreamPeerType.SUBSCRIBER,
      mediaConstraints = mediaConstraints,
      onIceCandidateRequest = { iceCandidate, _ ->
        signalingClient.sendCommand(
          SignalingCommand.ICE,
          "${iceCandidate.sdpMid}$ICE_SEPARATOR${iceCandidate.sdpMLineIndex}$ICE_SEPARATOR${iceCandidate.sdp}"
        )
      },
      onVideoTrack = { rtpTransceiver ->
        Log.v(TAG, "ontrack")
        val track = rtpTransceiver?.receiver?.track() ?: return@makePeerConnection
        if (track.kind() == MediaStreamTrack.VIDEO_TRACK_KIND) {
          Log.v(TAG, "ontrack inner")
          val videoTrack = track as VideoTrack
          sessionManagerScope.launch {
            remoteVideoTrackData.postValue(videoTrack)
          }
        }
      }
    )
  }

  init {
    sessionManagerScope.launch {
      Log.v(TAG, "init")
      signalingClient.signalingCommandFlow
        .collect { commandToValue ->
          when (commandToValue.first) {
            SignalingCommand.OFFER -> handleOffer(commandToValue.second)
            SignalingCommand.ANSWER -> handleAnswer(commandToValue.second)
            SignalingCommand.ICE -> handleIce(commandToValue.second)
            else -> Unit
          }
        }
    }
  }

  fun onSessionScreenReady() {
    setupAudio()
    peerConnection.connection.addTrack(localVideoTrack)
    peerConnection.connection.addTrack(localAudioTrack)
    sessionManagerScope.launch {
      // sending local video track to show local video from start
      localVideoTrackData.postValue(localVideoTrack)

      if (offer != null) {
        sendAnswer()
      } else {
        sendOffer()
      }
    }
  }

  fun flipCamera() {
    (videoCapturer as? Camera2Capturer)?.switchCamera(null)
  }

  fun enableMicrophone(enabled: Boolean) {
    localAudioTrack.setEnabled(enabled)
  }

  fun enableCamera(enabled: Boolean) {
    if (enabled) {
      videoCapturer.startCapture(resolution.width, resolution.height, 30)
    } else {
      videoCapturer.stopCapture()
    }
  }

  fun disconnect() {
    // dispose audio & video tracks.
    localAudioTrack.dispose()
    localVideoTrack.dispose()
    peerConnection.connection.dispose()

    // dispose audio handler and video capturer.
    audioHandler.stop()
    videoCapturer.stopCapture()
    videoCapturer.dispose()

    // dispose signaling clients and socket.
    signalingClient.dispose()

    // kill the handler thread
    handlerThread.quit()

    // delete the reference to the current instance
    (context as? ChatstoneApplication)?.deleteWebRTCManager()
  }

  private suspend fun sendOffer() {
    val offer = peerConnection.createOffer().getOrThrow()
    val result = peerConnection.setLocalDescription(offer)
    result.onSuccess {
      signalingClient.sendCommand(SignalingCommand.OFFER, offer.description)
    }
    Log.v(TAG, "[SDP] send offer: ${offer.stringify()}")
  }

  private suspend fun sendAnswer() {
    peerConnection.setRemoteDescription(
      SessionDescription(SessionDescription.Type.OFFER, offer)
    )
    val answer = peerConnection.createAnswer().getOrThrow()
    val result = peerConnection.setLocalDescription(answer)
    result.onSuccess {
      signalingClient.sendCommand(SignalingCommand.ANSWER, answer.description)
    }
    Log.v(TAG, "[SDP] send answer: ${answer.stringify()}")
  }

  private fun handleOffer(sdp: String) {
    Log.v(TAG, "[SDP] handle offer: $sdp")
    offer = sdp
  }

  private suspend fun handleAnswer(sdp: String) {
    Log.d(TAG, "[SDP] handle answer: $sdp")
    peerConnection.setRemoteDescription(
      SessionDescription(SessionDescription.Type.ANSWER, sdp)
    )
  }

  private suspend fun handleIce(iceMessage: String) {
    val iceArray = iceMessage.split(ICE_SEPARATOR)
    peerConnection.addIceCandidate(
      IceCandidate(
        iceArray[0],
        iceArray[1].toInt(),
        iceArray[2]
      )
    )
  }

  private fun buildCameraCapturer(): VideoCapturer {
    val manager = cameraManager ?: throw RuntimeException("CameraManager was not initialized!")

    val ids = manager.cameraIdList
    var foundCamera = false
    var cameraId = ""

    for (id in ids) {
      val characteristics = manager.getCameraCharacteristics(id)
      val cameraLensFacing = characteristics.get(CameraCharacteristics.LENS_FACING)

      if (cameraLensFacing == CameraMetadata.LENS_FACING_FRONT) {
        foundCamera = true
        cameraId = id
      }
    }

    if (!foundCamera && ids.isNotEmpty()) {
      cameraId = ids.first()
    }

    val camera2Capturer = Camera2Capturer(context, cameraId, null)
    return camera2Capturer
  }

  private fun buildAudioConstraints(): MediaConstraints {
    val mediaConstraints = MediaConstraints()
    val items = listOf(
      MediaConstraints.KeyValuePair(
        "googEchoCancellation",
        true.toString()
      ),
      MediaConstraints.KeyValuePair(
        "googAutoGainControl",
        true.toString()
      ),
      MediaConstraints.KeyValuePair(
        "googHighpassFilter",
        true.toString()
      ),
      MediaConstraints.KeyValuePair(
        "googNoiseSuppression",
        true.toString()
      ),
      MediaConstraints.KeyValuePair(
        "googTypingNoiseDetection",
        true.toString()
      )
    )

    return mediaConstraints.apply {
      with(optional) {
        add(MediaConstraints.KeyValuePair("DtlsSrtpKeyAgreement", "true"))
        addAll(items)
      }
    }
  }

  private fun setupAudio() {
//    logger.d { "[setupAudio] #sfu; no args" }
    audioHandler.start()
    audioManager?.mode = AudioManager.MODE_IN_COMMUNICATION

    val devices = audioManager?.availableCommunicationDevices ?: return
    val deviceType = AudioDeviceInfo.TYPE_BUILTIN_SPEAKER

    val device = devices.firstOrNull { it.type == deviceType } ?: return

    val isCommunicationDeviceSet = audioManager?.setCommunicationDevice(device)
//    logger.d { "[setupAudio] #sfu; isCommunicationDeviceSet: $isCommunicationDeviceSet" }
  }
}
