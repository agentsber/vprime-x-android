package com.vprimex.messenger.mediasend.v2

import android.net.Uri
import org.signal.core.models.media.Media
import com.vprimex.messenger.conversation.MessageSendType
import com.vprimex.messenger.keyvalue.SignalStore
import com.vprimex.messenger.mediasend.v2.videos.VideoTrimData
import com.vprimex.messenger.mms.MediaConstraints
import com.vprimex.messenger.mms.SentMediaQuality
import com.vprimex.messenger.recipients.Recipient
import com.vprimex.messenger.stories.Stories
import com.vprimex.messenger.util.MediaUtil
import com.vprimex.messenger.util.RemoteConfig
import com.vprimex.messenger.video.TranscodingPreset
import kotlin.time.Duration.Companion.seconds

data class MediaSelectionState(
  val sendType: MessageSendType,
  val selectedMedia: List<Media> = listOf(),
  val focusedMedia: Media? = null,
  val recipient: Recipient? = null,
  val quality: SentMediaQuality = SignalStore.settings.sentMediaQuality,
  val message: CharSequence? = null,
  val viewOnceToggleState: ViewOnceToggleState = ViewOnceToggleState.default,
  val isTouchEnabled: Boolean = true,
  val isSent: Boolean = false,
  val isPreUploadEnabled: Boolean = false,
  val isMeteredConnection: Boolean = false,
  val editorStateMap: Map<Uri, Any> = mapOf(),
  val cameraFirstCapture: Media? = null,
  val isStory: Boolean,
  val storySendRequirements: Stories.MediaTransform.SendRequirements = Stories.MediaTransform.SendRequirements.CAN_NOT_SEND,
  val suppressEmptyError: Boolean = true
) {

  val isVideoTrimmingVisible: Boolean = focusedMedia != null && MediaUtil.isVideoType(focusedMedia.contentType) && MediaConstraints.isVideoTranscodeAvailable() && !focusedMedia.isVideoGif

  val transcodingPreset: TranscodingPreset = MediaConstraints.getPushMediaConstraints(SentMediaQuality.fromCode(quality.code)).videoTranscodingSettings

  val maxSelection = RemoteConfig.maxAttachmentCount

  val canSend = !isSent && selectedMedia.isNotEmpty()

  fun getOrCreateVideoTrimData(uri: Uri): VideoTrimData {
    return editorStateMap[uri] as? VideoTrimData ?: VideoTrimData()
  }

  fun calculateMaxVideoDurationUs(maxFileSize: Long): Long {
    return if (isStory && !MediaConstraints.isVideoTranscodeAvailable()) {
      Stories.MAX_VIDEO_DURATION_MILLIS
    } else {
      transcodingPreset.calculateMaxVideoUploadDurationInSeconds(maxFileSize).seconds.inWholeMicroseconds
    }
  }

  enum class ViewOnceToggleState(val code: Int) {
    INFINITE(0),
    ONCE(1);

    fun next(): ViewOnceToggleState {
      return when (this) {
        INFINITE -> ONCE
        ONCE -> INFINITE
      }
    }

    companion object {
      val default = INFINITE

      fun fromCode(code: Int): ViewOnceToggleState {
        return when (code) {
          1 -> ONCE
          else -> INFINITE
        }
      }
    }
  }
}
