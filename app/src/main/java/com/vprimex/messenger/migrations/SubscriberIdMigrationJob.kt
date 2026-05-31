/*
 * Copyright 2024 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.vprimex.messenger.migrations

import com.vprimex.messenger.components.settings.app.subscription.InAppPaymentsRepository.toPaymentMethodType
import com.vprimex.messenger.database.SignalDatabase
import com.vprimex.messenger.database.model.InAppPaymentSubscriberRecord
import com.vprimex.messenger.jobmanager.Job
import com.vprimex.messenger.keyvalue.SignalStore
import java.util.Currency

/**
 * Migrates all subscriber ids from the key value store into the database.
 */
internal class SubscriberIdMigrationJob(
  parameters: Parameters = Parameters.Builder().build()
) : MigrationJob(
  parameters
) {

  companion object {
    const val KEY = "SubscriberIdMigrationJob"
  }

  override fun getFactoryKey(): String = KEY

  override fun isUiBlocking(): Boolean = false

  override fun performMigration() {
    Currency.getAvailableCurrencies().forEach { currency ->
      val subscriber = SignalStore.inAppPayments.getSubscriber(currency)

      if (subscriber != null) {
        SignalDatabase.inAppPaymentSubscribers.insertOrReplace(
          InAppPaymentSubscriberRecord(
            subscriberId = subscriber.subscriberId,
            currency = subscriber.currency,
            type = InAppPaymentSubscriberRecord.Type.DONATION,
            requiresCancel = SignalStore.inAppPayments.shouldCancelSubscriptionBeforeNextSubscribeAttempt,
            paymentMethodType = SignalStore.inAppPayments.getSubscriptionPaymentSourceType().toPaymentMethodType(),
            iapSubscriptionId = null
          )
        )
      }
    }
  }

  override fun shouldRetry(e: Exception): Boolean = false

  class Factory : Job.Factory<SubscriberIdMigrationJob> {
    override fun create(parameters: Parameters, serializedData: ByteArray?): SubscriberIdMigrationJob {
      return SubscriberIdMigrationJob(parameters)
    }
  }
}
