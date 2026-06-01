/*
 * Copyright 2025 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.vprimex.messenger.jobmanager.impl

import android.app.job.JobInfo
import com.vprimex.messenger.backup.DeletionState
import com.vprimex.messenger.jobmanager.Constraint
import com.vprimex.messenger.jobmanager.ConstraintObserver
import com.vprimex.messenger.keyvalue.SignalStore

/**
 * When we are awaiting media download, we want to suppress the running of the
 * deletion job such that once media *is* downloaded it can finish off deleting
 * the backup.
 */
object DeletionNotAwaitingMediaDownloadConstraint : Constraint {

  const val KEY = "DeletionNotAwaitingMediaDownloadConstraint"

  override fun isMet(): Boolean {
    return SignalStore.backup.deletionState != DeletionState.AWAITING_MEDIA_DOWNLOAD
  }

  override fun getFactoryKey(): String = KEY

  override fun applyToJobInfo(jobInfoBuilder: JobInfo.Builder) = Unit

  object Observer : ConstraintObserver {
    val listeners: MutableSet<ConstraintObserver.Notifier> = mutableSetOf()

    override fun register(notifier: ConstraintObserver.Notifier) {
      listeners += notifier
    }

    fun notifyListeners() {
      for (listener in listeners) {
        listener.onConstraintMet(KEY)
      }
    }
  }

  class Factory : Constraint.Factory<DeletionNotAwaitingMediaDownloadConstraint> {
    override fun create(): DeletionNotAwaitingMediaDownloadConstraint {
      return DeletionNotAwaitingMediaDownloadConstraint
    }
  }
}
