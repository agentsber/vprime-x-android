/*
 * Copyright 2026 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.vprimex.messenger.dependencies

import org.signal.core.util.CoreUtilDependencies
import com.vprimex.messenger.keyvalue.SignalStore
import com.vprimex.messenger.util.RemoteDeprecation

object CoreUtilDependenciesProvider : CoreUtilDependencies.Provider {
  override fun provideIsClientDeprecated(): Boolean {
    return SignalStore.misc.isClientDeprecated
  }

  override fun provideTimeUntilRemoteDeprecation(currentTime: Long): Long {
    return RemoteDeprecation.getTimeUntilDeprecation(currentTime)
  }
}
