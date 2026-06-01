package com.vprimex.messenger.stories.viewer.views

import com.vprimex.messenger.recipients.Recipient

data class StoryViewItemData(
  val recipient: Recipient,
  val timeViewedInMillis: Long
)
