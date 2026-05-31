package com.vprimex.messenger.database.model

import com.vprimex.messenger.recipients.RecipientId

/** A model for [com.vprimex.messenger.database.PendingRetryReceiptTable] */
data class PendingRetryReceiptModel(
  val id: Long,
  val author: RecipientId,
  val authorDevice: Int,
  val sentTimestamp: Long,
  val receivedTimestamp: Long,
  val threadId: Long
)
