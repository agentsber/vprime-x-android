/*
 * Copyright 2026 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.vprimex.messenger.components.settings.app.backups.local

import org.signal.core.models.AccountEntropyPool
import com.vprimex.messenger.components.settings.app.backups.remote.BackupKeySaveState
import com.vprimex.messenger.keyvalue.SignalStore

data class LocalBackupsKeyState(
  val accountEntropyPool: AccountEntropyPool = SignalStore.account.accountEntropyPool,
  val keySaveState: BackupKeySaveState? = null
)
