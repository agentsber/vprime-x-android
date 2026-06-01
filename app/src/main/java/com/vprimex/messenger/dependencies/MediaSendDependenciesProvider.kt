/*
 * Copyright 2026 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.vprimex.messenger.dependencies

import org.signal.mediasend.MediaSendDependencies
import org.signal.mediasend.MediaSendRepository
import org.signal.mediasend.preupload.PreUploadRepository
import com.vprimex.messenger.mediasend.v3.MediaSendV3PreUploadRepository
import com.vprimex.messenger.mediasend.v3.MediaSendV3Repository

object MediaSendDependenciesProvider : MediaSendDependencies.Provider {
  override fun provideMediaSendRepository(): MediaSendRepository = MediaSendV3Repository

  override fun providePreUploadRepository(): PreUploadRepository = MediaSendV3PreUploadRepository
}
