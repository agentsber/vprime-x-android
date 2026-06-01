package com.vprimex.messenger.components.settings.app.subscription.receipts.list

import com.vprimex.messenger.badges.models.Badge
import com.vprimex.messenger.database.model.InAppPaymentReceiptRecord

data class DonationReceiptBadge(
  val type: InAppPaymentReceiptRecord.Type,
  val level: Int,
  val badge: Badge
)
