package com.vprimex.messenger.service.webrtc

import org.signal.core.models.ServiceId.ACI
import org.signal.ringrtc.CallManager
import com.vprimex.messenger.groups.GroupId
import com.vprimex.messenger.recipients.RecipientId

data class GroupCallRingCheckInfo(
  val recipientId: RecipientId,
  val groupId: GroupId.V2,
  val ringId: Long,
  val ringerAci: ACI,
  val ringUpdate: CallManager.RingUpdate
)
