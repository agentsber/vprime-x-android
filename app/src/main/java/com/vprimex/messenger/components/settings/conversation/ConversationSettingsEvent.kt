package com.vprimex.messenger.components.settings.conversation

import com.vprimex.messenger.groups.GroupId
import com.vprimex.messenger.groups.SelectionLimits
import com.vprimex.messenger.groups.ui.GroupChangeFailureReason
import com.vprimex.messenger.recipients.Recipient
import com.vprimex.messenger.recipients.RecipientId

sealed class ConversationSettingsEvent {
  class AddToAGroup(
    val recipientId: RecipientId,
    val groupMembership: List<RecipientId>
  ) : ConversationSettingsEvent()

  class AddMembersToGroup(
    val groupId: GroupId,
    val selectionLimits: SelectionLimits,
    val groupMembersWithoutSelf: List<RecipientId>
  ) : ConversationSettingsEvent()

  object ShowGroupHardLimitDialog : ConversationSettingsEvent()

  class ShowAddMembersToGroupError(
    val failureReason: GroupChangeFailureReason
  ) : ConversationSettingsEvent()

  class ShowBlockGroupError(
    val failureReason: GroupChangeFailureReason
  ) : ConversationSettingsEvent()

  class ShowGroupInvitesSentDialog(
    val invitesSentTo: List<Recipient>
  ) : ConversationSettingsEvent()

  class ShowMembersAdded(
    val membersAddedCount: Int
  ) : ConversationSettingsEvent()
}
