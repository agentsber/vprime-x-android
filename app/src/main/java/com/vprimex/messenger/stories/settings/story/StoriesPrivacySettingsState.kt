package com.vprimex.messenger.stories.settings.story

import com.vprimex.messenger.contacts.paged.ContactSearchData
import com.vprimex.messenger.stories.archive.StoryArchiveDuration

data class StoriesPrivacySettingsState(
  val areStoriesEnabled: Boolean,
  val areViewReceiptsEnabled: Boolean,
  val isUpdatingEnabledState: Boolean = false,
  val storyContactItems: List<ContactSearchData> = emptyList(),
  val userHasStories: Boolean = false,
  val isArchiveEnabled: Boolean = false,
  val archiveDuration: StoryArchiveDuration = StoryArchiveDuration.THIRTY_DAYS
)
