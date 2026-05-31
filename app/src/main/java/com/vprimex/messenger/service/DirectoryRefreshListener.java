package com.vprimex.messenger.service;


import android.content.Context;

import com.vprimex.messenger.dependencies.AppDependencies;
import com.vprimex.messenger.jobs.DirectoryRefreshJob;
import com.vprimex.messenger.keyvalue.SignalStore;
import com.vprimex.messenger.util.RemoteConfig;
import com.vprimex.messenger.util.TextSecurePreferences;

import java.util.concurrent.TimeUnit;

public class DirectoryRefreshListener extends PersistentAlarmManagerListener {

  @Override
  protected long getNextScheduledExecutionTime(Context context) {
    return TextSecurePreferences.getDirectoryRefreshTime(context);
  }

  @Override
  protected long onAlarm(Context context, long scheduledTime) {
    if (scheduledTime != 0 && SignalStore.account().isRegistered()) {
      AppDependencies.getJobManager().add(new DirectoryRefreshJob(true));
    }

    long newTime;

    if (SignalStore.misc().isCdsBlocked()) {
      newTime = Math.min(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(6),
                         SignalStore.misc().getCdsBlockedUtil());
    } else {
      newTime = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(RemoteConfig.cdsRefreshIntervalSeconds());
      TextSecurePreferences.setDirectoryRefreshTime(context, newTime);
    }

    TextSecurePreferences.setDirectoryRefreshTime(context, newTime);

    return newTime;
  }

  public static void schedule(Context context) {
    new DirectoryRefreshListener().onReceive(context, getScheduleIntent());
  }
}
