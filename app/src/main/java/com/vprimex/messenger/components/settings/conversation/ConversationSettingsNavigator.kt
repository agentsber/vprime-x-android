/*
 * Copyright 2026 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.vprimex.messenger.components.settings.conversation

import androidx.fragment.app.FragmentActivity
import com.vprimex.messenger.main.MainNavigationChatDetailRouter
import com.vprimex.messenger.main.MainNavigationDetailLocation
import com.vprimex.messenger.recipients.Recipient

/**
 * Routes to the conversation settings screen, handling split-pane vs. standalone activity automatically.
 */
object ConversationSettingsNavigator {
  @JvmStatic
  fun navigate(
    activity: FragmentActivity,
    recipient: Recipient
  ) {
    if (activity is MainNavigationChatDetailRouter) {
      activity.goToChatDetail(MainNavigationDetailLocation.Chats.ConversationSettings(recipient.id))
      return
    }

    val intent = if (recipient.isPushGroup) {
      ConversationSettingsActivity.forGroup(activity, recipient.requireGroupId())
    } else {
      ConversationSettingsActivity.forRecipient(activity, recipient.id)
    }
    activity.startActivity(intent)
  }
}
