package com.vprimex.messenger.stories.settings.group

import com.vprimex.messenger.recipients.RecipientId

/**
 * Minimum data needed to launch ConversationActivity for a given grou
 */
data class GroupConversationData(
  val groupRecipientId: RecipientId,
  val groupThreadId: Long
)
