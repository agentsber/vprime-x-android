package com.vprimex.messenger.components.settings.app.subscription.receipts.list

import com.vprimex.messenger.database.model.InAppPaymentReceiptRecord

data class DonationReceiptListPageState(
  val records: List<InAppPaymentReceiptRecord> = emptyList(),
  val isLoaded: Boolean = false
)
