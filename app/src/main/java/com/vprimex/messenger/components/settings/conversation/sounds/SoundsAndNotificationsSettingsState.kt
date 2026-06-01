package com.vprimex.messenger.components.settings.conversation.sounds

import com.vprimex.messenger.database.RecipientTable
import com.vprimex.messenger.recipients.Recipient
import com.vprimex.messenger.recipients.RecipientId

data class SoundsAndNotificationsSettingsState(
  val recipientId: RecipientId = Recipient.UNKNOWN.id,
  val muteUntil: Long = 0L,
  val mentionSetting: RecipientTable.NotificationSetting = RecipientTable.NotificationSetting.DO_NOT_NOTIFY,
  val hasCustomNotificationSettings: Boolean = false,
  val hasMentionsSupport: Boolean = false,
  val channelConsistencyCheckComplete: Boolean = false
)
