/*
 * Copyright 2024 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.vprimex.messenger.registration.util;

import org.signal.core.util.logging.Log;
import com.vprimex.messenger.backup.v2.BackupRepository;
import com.vprimex.messenger.backup.v2.MessageBackupTier;
import com.vprimex.messenger.dependencies.AppDependencies;
import com.vprimex.messenger.jobs.ArchiveBackupIdReservationJob;
import com.vprimex.messenger.jobs.DirectoryRefreshJob;
import com.vprimex.messenger.jobs.EmojiSearchIndexDownloadJob;
import com.vprimex.messenger.jobs.PostRegistrationBackupRedemptionJob;
import com.vprimex.messenger.jobs.RefreshAttributesJob;
import com.vprimex.messenger.jobs.StorageSyncJob;
import com.vprimex.messenger.keyvalue.PhoneNumberPrivacyValues.PhoneNumberDiscoverabilityMode;
import com.vprimex.messenger.keyvalue.RestoreDecisionStateUtil;
import com.vprimex.messenger.keyvalue.SignalStore;
import com.vprimex.messenger.recipients.Recipient;
import com.vprimex.messenger.util.RemoteConfig;

public final class RegistrationUtil {

  private static final String TAG = Log.tag(RegistrationUtil.class);

  private RegistrationUtil() {}

  /**
   * There's several events where a registration may or may not be considered complete based on what
   * path a user has taken. This will only truly mark registration as complete if all of the
   * requirements are met.
   */
  public static void maybeMarkRegistrationComplete() {
    if (!SignalStore.registration().isRegistrationComplete() &&
        SignalStore.account().isRegistered() &&
        !Recipient.self().getProfileName().isEmpty() &&
        (SignalStore.svr().hasPin() || SignalStore.svr().hasOptedOut() || SignalStore.account().isLinkedDevice()) &&
        RestoreDecisionStateUtil.isTerminal(SignalStore.registration().getRestoreDecisionState()))
    {
      Log.i(TAG, "Marking registration completed.", new Throwable());
      SignalStore.registration().markRegistrationComplete();
      SignalStore.registration().setLocalRegistrationMetadata(null);
      SignalStore.registration().setRestoreMethodToken(null);

      if (SignalStore.phoneNumberPrivacy().getPhoneNumberDiscoverabilityMode() == PhoneNumberDiscoverabilityMode.UNDECIDED) {
        Log.w(TAG, "Phone number discoverability mode is still UNDECIDED. Setting to DISCOVERABLE.");
        SignalStore.phoneNumberPrivacy().setPhoneNumberDiscoverabilityMode(PhoneNumberDiscoverabilityMode.DISCOVERABLE);
      }

      AppDependencies.getJobManager().startChain(new RefreshAttributesJob())
                     .then(StorageSyncJob.forRemoteChange())
                     .then(new DirectoryRefreshJob(false))
                     .enqueue();

      SignalStore.emoji().clearSearchIndexMetadata();
      EmojiSearchIndexDownloadJob.scheduleImmediately();


      BackupRepository.INSTANCE.resetInitializedStateAndAuthCredentials();
      AppDependencies.getJobManager().add(new ArchiveBackupIdReservationJob());
      AppDependencies.getJobManager().add(new PostRegistrationBackupRedemptionJob());

    } else if (!SignalStore.registration().isRegistrationComplete()) {
      Log.i(TAG, "Registration is not yet complete.", new Throwable());
    }
  }
}
