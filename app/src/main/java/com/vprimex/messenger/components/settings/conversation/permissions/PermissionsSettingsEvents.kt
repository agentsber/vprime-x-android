package com.vprimex.messenger.components.settings.conversation.permissions

import com.vprimex.messenger.groups.ui.GroupChangeFailureReason

sealed class PermissionsSettingsEvents {
  class GroupChangeError(val reason: GroupChangeFailureReason) : PermissionsSettingsEvents()
  object ShowMemberLabelsWillBeRemovedWarning : PermissionsSettingsEvents()
}
