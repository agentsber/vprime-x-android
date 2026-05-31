package com.vprimex.messenger.components.settings.app.subscription.manage

import com.vprimex.messenger.badges.models.Badge
import com.vprimex.messenger.database.InAppPaymentTable
import com.vprimex.messenger.database.model.databaseprotos.PendingOneTimeDonation
import com.vprimex.messenger.subscription.Subscription

data class ManageDonationsState(
  val hasOneTimeBadge: Boolean = false,
  val hasReceipts: Boolean = false,
  val featuredBadge: Badge? = null,
  val isLoaded: Boolean = false,
  val networkError: Boolean = false,
  val availableSubscriptions: List<Subscription> = emptyList(),
  val activeSubscription: InAppPaymentTable.InAppPayment? = null,
  val subscriptionRedemptionState: RedemptionState = RedemptionState.NONE,
  val pendingOneTimeDonation: PendingOneTimeDonation? = null,
  val nonVerifiedMonthlyDonation: NonVerifiedMonthlyDonation? = null,
  val subscriberRequiresCancel: Boolean = false
) {

  enum class RedemptionState {
    NONE,
    IN_PROGRESS,
    SUBSCRIPTION_REFRESH,
    IS_PENDING_BANK_TRANSFER,
    FAILED
  }
}
