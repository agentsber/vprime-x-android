/*
 * Copyright 2025 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.vprimex.messenger.main

import org.signal.core.util.concurrent.SignalExecutors
import com.vprimex.messenger.database.SignalDatabase
import com.vprimex.messenger.dependencies.AppDependencies
import com.vprimex.messenger.notifications.MarkReadReceiver

object MainToolbarRepository {
  /**
   * Mark all unread messages in the local database as read.
   */
  fun markAllMessagesRead() {
    SignalExecutors.BOUNDED.execute {
      val messageIds = SignalDatabase.threads.setAllThreadsRead()
      AppDependencies.messageNotifier.updateNotification(AppDependencies.application)
      MarkReadReceiver.process(messageIds)
    }
  }
}
