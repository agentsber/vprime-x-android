package com.vprimex.messenger.badges.gifts.viewgift.sent

import com.vprimex.messenger.badges.models.Badge
import com.vprimex.messenger.recipients.Recipient

data class ViewSentGiftState(
  val recipient: Recipient? = null,
  val badge: Badge? = null
)
