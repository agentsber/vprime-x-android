package com.vprimex.messenger.badges.view

import com.vprimex.messenger.badges.models.Badge
import com.vprimex.messenger.recipients.Recipient

data class ViewBadgeState(
  val allBadgesVisibleOnProfile: List<Badge> = listOf(),
  val badgeLoadState: LoadState = LoadState.INIT,
  val selectedBadge: Badge? = null,
  val recipient: Recipient? = null
) {
  enum class LoadState {
    INIT,
    LOADED
  }
}
