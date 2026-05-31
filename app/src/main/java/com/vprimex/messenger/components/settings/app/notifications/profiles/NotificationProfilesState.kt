package com.vprimex.messenger.components.settings.app.notifications.profiles

import com.vprimex.messenger.notifications.profiles.NotificationProfile
import com.vprimex.messenger.notifications.profiles.NotificationProfiles

data class NotificationProfilesState(
  val profiles: List<NotificationProfile>,
  val activeProfile: NotificationProfile? = NotificationProfiles.getActiveProfile(profiles)
)
