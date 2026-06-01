package com.vprimex.messenger.database.model

import org.signal.libsignal.protocol.IdentityKey
import com.vprimex.messenger.database.IdentityTable
import com.vprimex.messenger.recipients.RecipientId

data class IdentityRecord(
  val recipientId: RecipientId,
  val identityKey: IdentityKey,
  val verifiedStatus: IdentityTable.VerifiedStatus,
  @get:JvmName("isFirstUse")
  val firstUse: Boolean,
  val timestamp: Long,
  @get:JvmName("isApprovedNonBlocking")
  val nonblockingApproval: Boolean
)
