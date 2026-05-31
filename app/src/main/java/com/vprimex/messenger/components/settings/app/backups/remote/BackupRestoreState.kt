/*
 * Copyright 2024 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.vprimex.messenger.components.settings.app.backups.remote

import com.vprimex.messenger.backup.v2.ArchiveRestoreProgressState

/**
 * State container for BackupStatusData, including the enabled state.
 */
sealed interface BackupRestoreState {
  data object None : BackupRestoreState
  data class Ready(val bytes: String) : BackupRestoreState
  data class Restoring(val state: ArchiveRestoreProgressState) : BackupRestoreState
}
