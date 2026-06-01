/*
 * Copyright 2026 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.vprimex.messenger.dependencies

import android.net.Uri
import org.signal.glide.SignalGlideDependencies
import org.signal.glide.common.io.InputStreamFactory
import com.vprimex.messenger.glide.DecryptableStreamFactory

object SignalGlideDependenciesProvider : SignalGlideDependencies.Provider {
  override fun getUriInputStreamFactory(uri: Uri, thumbnailTimeUs: Long): InputStreamFactory {
    return DecryptableStreamFactory(uri, thumbnailTimeUs)
  }
}
