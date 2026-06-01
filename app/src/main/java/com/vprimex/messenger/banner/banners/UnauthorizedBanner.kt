/*
 * Copyright 2024 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.vprimex.messenger.banner.banners

import android.content.Context
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.signal.core.ui.compose.DayNightPreviews
import org.signal.core.ui.compose.Previews
import com.vprimex.messenger.R
import com.vprimex.messenger.banner.Banner
import com.vprimex.messenger.banner.ui.compose.Action
import com.vprimex.messenger.banner.ui.compose.DefaultBanner
import com.vprimex.messenger.banner.ui.compose.Importance
import com.vprimex.messenger.keyvalue.SignalStore
import com.vprimex.messenger.registration.ui.RegistrationActivity
import com.vprimex.messenger.util.TextSecurePreferences

/**
 * A banner displayed when the client is unauthorized (deregistered).
 */
class UnauthorizedBanner(val context: Context) : Banner<Unit>() {

  override val enabled: Boolean
    get() = TextSecurePreferences.isUnauthorizedReceived(context) || !SignalStore.account.isRegistered

  override val dataFlow: Flow<Unit>
    get() = flowOf(Unit)

  @Composable
  override fun DisplayBanner(model: Unit, contentPadding: PaddingValues) {
    Banner(contentPadding)
  }
}

@Composable
private fun Banner(contentPadding: PaddingValues) {
  val context = LocalContext.current

  DefaultBanner(
    title = null,
    body = stringResource(id = R.string.UnauthorizedReminder_this_is_likely_because_you_registered_your_phone_number_with_Signal_on_a_different_device),
    importance = Importance.ERROR,
    actions = listOf(
      Action(R.string.UnauthorizedReminder_reregister_action) {
        val registrationIntent = RegistrationActivity.newIntentForReRegistration(context)
        context.startActivity(registrationIntent)
      }
    ),
    paddingValues = contentPadding
  )
}

@DayNightPreviews
@Composable
private fun BannerPreview() {
  Previews.Preview {
    Banner(PaddingValues(0.dp))
  }
}
