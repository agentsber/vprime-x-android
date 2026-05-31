/*
 * Copyright 2026 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.vprimex.messenger.dependencies

import org.signal.core.ui.CoreUiDependencies
import com.vprimex.messenger.BuildConfig
import com.vprimex.messenger.keyvalue.SignalStore
import com.vprimex.messenger.util.TextSecurePreferences

object CoreUiDependenciesProvider : CoreUiDependencies.Provider {
  override fun providePackageId(): String {
    return BuildConfig.APPLICATION_ID
  }

  override fun provideIsIncognitoKeyboardEnabled(): Boolean {
    return TextSecurePreferences.isIncognitoKeyboardEnabled(AppDependencies.application)
  }

  override fun provideIsScreenSecurityEnabled(): Boolean {
    return TextSecurePreferences.isScreenSecurityEnabled(AppDependencies.application)
  }

  override fun provideForceSplitPane(): Boolean {
    return SignalStore.internal.forceSplitPane
  }
}
