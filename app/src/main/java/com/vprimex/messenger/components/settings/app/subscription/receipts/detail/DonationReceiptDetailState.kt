package com.vprimex.messenger.components.settings.app.subscription.receipts.detail

import com.vprimex.messenger.database.model.InAppPaymentReceiptRecord

data class DonationReceiptDetailState(
  val inAppPaymentReceiptRecord: InAppPaymentReceiptRecord? = null
)
