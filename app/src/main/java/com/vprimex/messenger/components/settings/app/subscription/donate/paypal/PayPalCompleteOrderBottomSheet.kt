package com.vprimex.messenger.components.settings.app.subscription.donate.paypal

import android.content.DialogInterface
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.signal.core.util.concurrent.SignalDispatchers
import org.signal.core.util.dp
import com.vprimex.messenger.R
import com.vprimex.messenger.badges.Badges
import com.vprimex.messenger.badges.models.BadgeDisplay112
import com.vprimex.messenger.components.settings.DSLConfiguration
import com.vprimex.messenger.components.settings.DSLSettingsAdapter
import com.vprimex.messenger.components.settings.DSLSettingsBottomSheetFragment
import com.vprimex.messenger.components.settings.DSLSettingsText
import com.vprimex.messenger.components.settings.app.subscription.donate.gateway.GatewaySelectorBottomSheet.Companion.presentTitleAndSubtitle
import com.vprimex.messenger.components.settings.configure
import com.vprimex.messenger.database.InAppPaymentTable
import com.vprimex.messenger.database.SignalDatabase

/**
 * Bottom sheet for final order confirmation from PayPal
 */
class PayPalCompleteOrderBottomSheet : DSLSettingsBottomSheetFragment() {

  companion object {
    const val REQUEST_KEY = "complete_order"
  }

  private var didConfirmOrder = false
  private val args: PayPalCompleteOrderBottomSheetArgs by navArgs()

  override fun bindAdapter(adapter: DSLSettingsAdapter) {
    BadgeDisplay112.register(adapter)
    PayPalCompleteOrderPaymentItem.register(adapter)

    lifecycleScope.launch {
      val inAppPayment = withContext(SignalDispatchers.IO) {
        SignalDatabase.inAppPayments.getById(args.inAppPaymentId)!!
      }

      adapter.submitList(getConfiguration(inAppPayment).toMappingModelList())
    }
  }

  override fun onDismiss(dialog: DialogInterface) {
    super.onDismiss(dialog)
    setFragmentResult(REQUEST_KEY, bundleOf(REQUEST_KEY to didConfirmOrder))
  }

  private fun getConfiguration(inAppPayment: InAppPaymentTable.InAppPayment): DSLConfiguration {
    return configure {
      customPref(
        BadgeDisplay112.Model(
          badge = Badges.fromDatabaseBadge(inAppPayment.data.badge!!),
          withDisplayText = false
        )
      )

      space(12.dp)

      presentTitleAndSubtitle(requireContext(), inAppPayment)

      space(24.dp)

      customPref(PayPalCompleteOrderPaymentItem.Model())

      space(82.dp)

      primaryButton(
        text = DSLSettingsText.from(R.string.PaypalCompleteOrderBottomSheet__donate),
        onClick = {
          didConfirmOrder = true
          findNavController().popBackStack()
        }
      )

      secondaryButtonNoOutline(
        text = DSLSettingsText.from(android.R.string.cancel),
        onClick = {
          findNavController().popBackStack()
        }
      )

      space(16.dp)
    }
  }
}
