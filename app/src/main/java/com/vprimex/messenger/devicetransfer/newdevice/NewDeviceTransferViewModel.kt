/*
 * Copyright 2024 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.vprimex.messenger.devicetransfer.newdevice

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.vprimex.messenger.database.model.databaseprotos.RestoreDecisionState
import com.vprimex.messenger.dependencies.AppDependencies
import com.vprimex.messenger.jobs.ReclaimUsernameAndLinkJob
import com.vprimex.messenger.keyvalue.Completed
import com.vprimex.messenger.keyvalue.SignalStore
import com.vprimex.messenger.registration.data.RegistrationRepository
import com.vprimex.messenger.registration.util.RegistrationUtil

class NewDeviceTransferViewModel : ViewModel() {
  fun onRestoreComplete(context: Context, onComplete: () -> Unit) {
    viewModelScope.launch {
      SignalStore.registration.localRegistrationMetadata?.let { metadata ->
        RegistrationRepository.registerAccountLocally(context, metadata)
        SignalStore.registration.localRegistrationMetadata = null
        RegistrationUtil.maybeMarkRegistrationComplete()

        SignalStore.misc.needsUsernameRestore = true
        AppDependencies.jobManager.add(ReclaimUsernameAndLinkJob())
      }

      SignalStore.registration.restoreDecisionState = RestoreDecisionState.Completed

      withContext(Dispatchers.Main) {
        onComplete()
      }
    }
  }
}
