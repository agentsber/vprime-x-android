package com.vprimex.messenger.profiles.edit.pnp

import io.reactivex.rxjava3.core.Completable
import com.vprimex.messenger.dependencies.AppDependencies
import com.vprimex.messenger.jobs.ProfileUploadJob
import com.vprimex.messenger.jobs.RefreshAttributesJob
import com.vprimex.messenger.keyvalue.PhoneNumberPrivacyValues
import com.vprimex.messenger.keyvalue.SignalStore
import com.vprimex.messenger.storage.StorageSyncHelper

/**
 * Manages the current phone-number listing state.
 */
class WhoCanFindMeByPhoneNumberRepository {

  fun getCurrentState(): WhoCanFindMeByPhoneNumberState {
    return when (SignalStore.phoneNumberPrivacy.phoneNumberDiscoverabilityMode) {
      PhoneNumberPrivacyValues.PhoneNumberDiscoverabilityMode.DISCOVERABLE -> WhoCanFindMeByPhoneNumberState.EVERYONE
      PhoneNumberPrivacyValues.PhoneNumberDiscoverabilityMode.NOT_DISCOVERABLE -> WhoCanFindMeByPhoneNumberState.NOBODY
      PhoneNumberPrivacyValues.PhoneNumberDiscoverabilityMode.UNDECIDED -> WhoCanFindMeByPhoneNumberState.EVERYONE
    }
  }

  fun onSave(whoCanFindMeByPhoneNumberState: WhoCanFindMeByPhoneNumberState): Completable {
    return Completable.fromAction {
      when (whoCanFindMeByPhoneNumberState) {
        WhoCanFindMeByPhoneNumberState.EVERYONE -> {
          SignalStore.phoneNumberPrivacy.phoneNumberDiscoverabilityMode = PhoneNumberPrivacyValues.PhoneNumberDiscoverabilityMode.DISCOVERABLE
        }
        WhoCanFindMeByPhoneNumberState.NOBODY -> {
          SignalStore.phoneNumberPrivacy.phoneNumberSharingMode = PhoneNumberPrivacyValues.PhoneNumberSharingMode.NOBODY
          SignalStore.phoneNumberPrivacy.phoneNumberDiscoverabilityMode = PhoneNumberPrivacyValues.PhoneNumberDiscoverabilityMode.NOT_DISCOVERABLE
        }
      }

      AppDependencies.jobManager.add(RefreshAttributesJob())
      StorageSyncHelper.scheduleSyncForDataChange()
      AppDependencies.jobManager.add(ProfileUploadJob())
    }
  }
}
