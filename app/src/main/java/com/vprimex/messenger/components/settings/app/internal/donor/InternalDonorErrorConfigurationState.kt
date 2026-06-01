package com.vprimex.messenger.components.settings.app.internal.donor

import org.signal.donations.StripeDeclineCode
import com.vprimex.messenger.badges.models.Badge
import com.vprimex.messenger.components.settings.app.subscription.errors.UnexpectedSubscriptionCancellation

data class InternalDonorErrorConfigurationState(
  val badges: List<Badge> = emptyList(),
  val selectedBadge: Badge? = null,
  val selectedUnexpectedSubscriptionCancellation: UnexpectedSubscriptionCancellation? = null,
  val selectedStripeDeclineCode: StripeDeclineCode.Code? = null
)
