package com.vprimex.messenger.stories.settings.group

import com.vprimex.messenger.recipients.Recipient

data class GroupStorySettingsState(
  val name: String = "",
  val members: List<Recipient> = emptyList(),
  val removed: Boolean = false
)
