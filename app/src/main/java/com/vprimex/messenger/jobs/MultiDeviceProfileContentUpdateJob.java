package com.vprimex.messenger.jobs;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.signal.core.util.logging.Log;
import com.vprimex.messenger.dependencies.AppDependencies;
import com.vprimex.messenger.jobmanager.Job;
import com.vprimex.messenger.jobmanager.impl.NetworkConstraint;
import com.vprimex.messenger.jobmanager.impl.SealedSenderConstraint;
import com.vprimex.messenger.keyvalue.SignalStore;
import com.vprimex.messenger.net.NotPushRegisteredException;
import com.vprimex.messenger.recipients.Recipient;
import com.vprimex.messenger.util.TextSecurePreferences;
import org.whispersystems.signalservice.api.SignalServiceMessageSender;
import org.whispersystems.signalservice.api.messages.multidevice.SignalServiceSyncMessage;
import org.signal.network.exceptions.PushNetworkException;
import org.whispersystems.signalservice.api.push.exceptions.ServerRejectedException;

public class MultiDeviceProfileContentUpdateJob extends BaseJob {

  public static final String KEY = "MultiDeviceProfileContentUpdateJob";

  private static final String TAG = Log.tag(MultiDeviceProfileContentUpdateJob.class);

  public MultiDeviceProfileContentUpdateJob() {
    this(new Parameters.Builder()
                       .setQueue("MultiDeviceProfileUpdateJob")
                       .setMaxInstancesForFactory(2)
                       .addConstraint(NetworkConstraint.KEY)
                       .addConstraint(SealedSenderConstraint.KEY)
                       .setMaxAttempts(10)
                       .build());
  }

  private MultiDeviceProfileContentUpdateJob(@NonNull Parameters parameters) {
    super(parameters);
  }

  @Override
  public @Nullable byte[] serialize() {
    return null;
  }

  @Override
  public @NonNull String getFactoryKey() {
    return KEY;
  }

  @Override
  protected void onRun() throws Exception {
    if (!Recipient.self().isRegistered()) {
      throw new NotPushRegisteredException();
    }

    if (!SignalStore.account().isMultiDevice()) {
      Log.i(TAG, "Not multi device, aborting...");
      return;
    }

    SignalServiceMessageSender messageSender = AppDependencies.getSignalServiceMessageSender();

    messageSender.sendSyncMessage(SignalServiceSyncMessage.forFetchLatest(SignalServiceSyncMessage.FetchType.LOCAL_PROFILE)
    );
  }

  @Override
  protected boolean onShouldRetry(@NonNull Exception e) {
    if (e instanceof ServerRejectedException) return false;
    return e instanceof PushNetworkException;
  }

  @Override
  public void onFailure() {
    Log.w(TAG, "Did not succeed!");
  }

  public static final class Factory implements Job.Factory<MultiDeviceProfileContentUpdateJob> {
    @Override
    public @NonNull MultiDeviceProfileContentUpdateJob create(@NonNull Parameters parameters, @Nullable byte[] serializedData) {
      return new MultiDeviceProfileContentUpdateJob(parameters);
    }
  }
}
