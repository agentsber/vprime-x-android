package com.vprimex.messenger.badges.gifts.viewgift

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import org.signal.libsignal.zkgroup.receipts.ReceiptCredentialPresentation
import com.vprimex.messenger.badges.models.Badge
import com.vprimex.messenger.components.settings.app.subscription.getBadge
import com.vprimex.messenger.database.DatabaseObserver
import com.vprimex.messenger.database.SignalDatabase
import com.vprimex.messenger.database.model.MmsMessageRecord
import com.vprimex.messenger.database.model.databaseprotos.GiftBadge
import com.vprimex.messenger.dependencies.AppDependencies
import java.util.Locale

/**
 * Shared repository for getting information about a particular gift.
 */
class ViewGiftRepository {
  fun getBadge(giftBadge: GiftBadge): Single<Badge> {
    val presentation = ReceiptCredentialPresentation(giftBadge.redemptionToken.toByteArray())
    return Single
      .fromCallable {
        AppDependencies
          .donationsService
          .getDonationsConfiguration(Locale.getDefault())
      }
      .flatMap { it.flattenResult() }
      .map { it.getBadge(presentation.receiptLevel.toInt()) }
      .subscribeOn(Schedulers.io())
  }

  fun getGiftBadge(messageId: Long): Observable<GiftBadge> {
    return Observable.create { emitter ->
      fun refresh() {
        val record = SignalDatabase.messages.getMessageRecord(messageId)
        val giftBadge: GiftBadge = (record as MmsMessageRecord).giftBadge!!

        emitter.onNext(giftBadge)
      }

      val messageObserver = DatabaseObserver.MessageObserver {
        if (messageId == it.id) {
          refresh()
        }
      }

      AppDependencies.databaseObserver.registerMessageUpdateObserver(messageObserver)
      emitter.setCancellable {
        AppDependencies.databaseObserver.unregisterObserver(messageObserver)
      }

      refresh()
    }
  }
}
