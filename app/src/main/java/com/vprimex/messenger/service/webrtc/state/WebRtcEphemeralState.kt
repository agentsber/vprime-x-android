package com.vprimex.messenger.service.webrtc.state

import com.vprimex.messenger.events.CallParticipant
import com.vprimex.messenger.events.CallParticipantId
import com.vprimex.messenger.events.GroupCallReactionEvent

/**
 * The state of the call system which contains data which changes frequently.
 */
data class WebRtcEphemeralState(
  val localAudioLevel: CallParticipant.AudioLevel = CallParticipant.AudioLevel.LOWEST,
  val remoteAudioLevels: Map<CallParticipantId, CallParticipant.AudioLevel> = emptyMap(),
  private val reactions: List<GroupCallReactionEvent> = emptyList()
) {

  fun getUnexpiredReactions(): List<GroupCallReactionEvent> {
    return reactions.filter { System.currentTimeMillis() < it.getExpirationTimestamp() }
  }
}
