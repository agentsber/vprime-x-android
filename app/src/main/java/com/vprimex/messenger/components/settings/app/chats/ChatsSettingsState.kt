package com.vprimex.messenger.components.settings.app.chats

import com.vprimex.messenger.backup.LocalExportProgress
import com.vprimex.messenger.keyvalue.protos.LocalBackupCreationProgress

data class ChatsSettingsState(
  val generateLinkPreviews: Boolean,
  val useAddressBook: Boolean,
  val keepMutedChatsArchived: Boolean,
  val useSystemEmoji: Boolean,
  val enterKeySends: Boolean,
  val localBackupsEnabled: Boolean,
  val folderCount: Int,
  val userUnregistered: Boolean,
  val clientDeprecated: Boolean,
  val isPlaintextExportEnabled: Boolean,
  val plaintextExportProgress: LocalBackupCreationProgress = LocalExportProgress.plaintextProgress.value,
  val chatExportState: ChatExportState = ChatExportState.None,
  val includeMediaInExport: Boolean = false
) {
  fun isRegisteredAndUpToDate(): Boolean {
    return !userUnregistered && !clientDeprecated
  }
}
