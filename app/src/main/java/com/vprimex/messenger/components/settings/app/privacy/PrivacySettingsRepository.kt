package com.vprimex.messenger.components.settings.app.privacy

import android.content.Context
import org.signal.core.util.concurrent.SignalExecutors
import com.vprimex.messenger.database.SignalDatabase
import com.vprimex.messenger.dependencies.AppDependencies
import com.vprimex.messenger.jobs.MultiDeviceConfigurationUpdateJob
import com.vprimex.messenger.keyvalue.SignalStore
import com.vprimex.messenger.recipients.Recipient
import com.vprimex.messenger.storage.StorageSyncHelper
import com.vprimex.messenger.util.TextSecurePreferences

class PrivacySettingsRepository {

  private val context: Context = AppDependencies.application

  fun getBlockedCount(consumer: (Int) -> Unit) {
    SignalExecutors.BOUNDED.execute {
      val recipientDatabase = SignalDatabase.recipients

      consumer(recipientDatabase.getBlocked().size)
    }
  }

  fun syncReadReceiptState() {
    SignalExecutors.BOUNDED.execute {
      SignalDatabase.recipients.markNeedsSync(Recipient.self().id)
      StorageSyncHelper.scheduleSyncForDataChange()
      AppDependencies.jobManager.add(
        MultiDeviceConfigurationUpdateJob(
          TextSecurePreferences.isReadReceiptsEnabled(context),
          TextSecurePreferences.isTypingIndicatorsEnabled(context),
          TextSecurePreferences.isShowUnidentifiedDeliveryIndicatorsEnabled(context),
          SignalStore.settings.isLinkPreviewsEnabled
        )
      )
    }
  }

  fun syncTypingIndicatorsState() {
    val enabled = TextSecurePreferences.isTypingIndicatorsEnabled(context)

    SignalDatabase.recipients.markNeedsSync(Recipient.self().id)
    StorageSyncHelper.scheduleSyncForDataChange()
    AppDependencies.jobManager.add(
      MultiDeviceConfigurationUpdateJob(
        TextSecurePreferences.isReadReceiptsEnabled(context),
        enabled,
        TextSecurePreferences.isShowUnidentifiedDeliveryIndicatorsEnabled(context),
        SignalStore.settings.isLinkPreviewsEnabled
      )
    )

    if (!enabled) {
      AppDependencies.typingStatusRepository.clear()
    }
  }
}
