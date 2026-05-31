package com.vprimex.messenger.components.settings.app.chats

import android.content.Context
import org.signal.core.util.concurrent.SignalExecutors
import com.vprimex.messenger.database.SignalDatabase
import com.vprimex.messenger.dependencies.AppDependencies
import com.vprimex.messenger.jobs.MultiDeviceConfigurationUpdateJob
import com.vprimex.messenger.jobs.MultiDeviceContactUpdateJob
import com.vprimex.messenger.keyvalue.SignalStore
import com.vprimex.messenger.recipients.Recipient
import com.vprimex.messenger.storage.StorageSyncHelper
import com.vprimex.messenger.util.TextSecurePreferences

class ChatsSettingsRepository {

  private val context: Context = AppDependencies.application

  fun syncLinkPreviewsState() {
    SignalExecutors.BOUNDED.execute {
      val isLinkPreviewsEnabled = SignalStore.settings.isLinkPreviewsEnabled

      SignalDatabase.recipients.markNeedsSync(Recipient.self().id)
      StorageSyncHelper.scheduleSyncForDataChange()
      AppDependencies.jobManager.add(
        MultiDeviceConfigurationUpdateJob(
          TextSecurePreferences.isReadReceiptsEnabled(context),
          TextSecurePreferences.isTypingIndicatorsEnabled(context),
          TextSecurePreferences.isShowUnidentifiedDeliveryIndicatorsEnabled(context),
          isLinkPreviewsEnabled
        )
      )
    }
  }

  fun syncPreferSystemContactPhotos() {
    SignalExecutors.BOUNDED.execute {
      SignalDatabase.recipients.markNeedsSync(Recipient.self().id)
      AppDependencies.jobManager.add(MultiDeviceContactUpdateJob(true))
      StorageSyncHelper.scheduleSyncForDataChange()
    }
  }

  fun syncKeepMutedChatsArchivedState() {
    SignalExecutors.BOUNDED.execute {
      SignalDatabase.recipients.markNeedsSync(Recipient.self().id)
      StorageSyncHelper.scheduleSyncForDataChange()
    }
  }
}
