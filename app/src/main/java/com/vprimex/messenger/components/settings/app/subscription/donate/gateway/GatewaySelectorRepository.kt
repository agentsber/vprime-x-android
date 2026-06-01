package com.vprimex.messenger.components.settings.app.subscription.donate.gateway

import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.signal.core.util.money.FiatMoney
import com.vprimex.messenger.components.settings.app.subscription.getAvailablePaymentMethods
import com.vprimex.messenger.database.InAppPaymentTable
import com.vprimex.messenger.database.SignalDatabase
import com.vprimex.messenger.database.model.databaseprotos.InAppPaymentData
import com.vprimex.messenger.dependencies.AppDependencies
import com.vprimex.messenger.payments.currency.CurrencyUtil
import org.whispersystems.signalservice.internal.push.SubscriptionsConfiguration
import java.util.Locale

object GatewaySelectorRepository {
  fun getAvailableGatewayConfiguration(currencyCode: String): Single<GatewayConfiguration> {
    return Single.fromCallable {
      AppDependencies.donationsService.getDonationsConfiguration(Locale.getDefault())
    }.flatMap { it.flattenResult() }
      .map { configuration ->
        val available = configuration.getAvailablePaymentMethods(currencyCode).map {
          when (it) {
            SubscriptionsConfiguration.PAYPAL -> listOf(InAppPaymentData.PaymentMethodType.PAYPAL)
            SubscriptionsConfiguration.CARD -> listOf(InAppPaymentData.PaymentMethodType.CARD, InAppPaymentData.PaymentMethodType.GOOGLE_PAY)
            SubscriptionsConfiguration.SEPA_DEBIT -> listOf(InAppPaymentData.PaymentMethodType.SEPA_DEBIT)
            SubscriptionsConfiguration.IDEAL -> listOf(InAppPaymentData.PaymentMethodType.IDEAL)
            else -> listOf()
          }
        }.flatten().toSet()

        GatewayConfiguration(
          availableGateways = available,
          sepaEuroMaximum = if (configuration.sepaMaximumEuros != null) FiatMoney(configuration.sepaMaximumEuros, CurrencyUtil.EURO) else null
        )
      }
  }

  suspend fun setInAppPaymentMethodType(inAppPayment: InAppPaymentTable.InAppPayment, paymentMethodType: InAppPaymentData.PaymentMethodType): InAppPaymentTable.InAppPayment {
    return withContext(Dispatchers.IO) {
      SignalDatabase.inAppPayments.update(
        inAppPayment.copy(
          data = inAppPayment.data.copy(
            paymentMethodType = paymentMethodType
          )
        )
      )

      SignalDatabase.inAppPayments.getById(inAppPayment.id) ?: throw Exception("Not found.")
    }
  }

  data class GatewayConfiguration(
    val availableGateways: Set<InAppPaymentData.PaymentMethodType>,
    val sepaEuroMaximum: FiatMoney?
  )
}
