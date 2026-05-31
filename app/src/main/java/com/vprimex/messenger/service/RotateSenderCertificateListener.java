package com.vprimex.messenger.service;


import android.content.Context;

import com.vprimex.messenger.dependencies.AppDependencies;
import com.vprimex.messenger.jobs.RotateCertificateJob;
import com.vprimex.messenger.util.TextSecurePreferences;

import java.util.concurrent.TimeUnit;

public class RotateSenderCertificateListener extends PersistentAlarmManagerListener {

  private static final long INTERVAL = TimeUnit.DAYS.toMillis(1);

  @Override
  protected long getNextScheduledExecutionTime(Context context) {
    return TextSecurePreferences.getUnidentifiedAccessCertificateRotationTime(context);
  }

  @Override
  protected long onAlarm(Context context, long scheduledTime) {
    AppDependencies.getJobManager().add(new RotateCertificateJob());

    long nextTime = System.currentTimeMillis() + INTERVAL;
    TextSecurePreferences.setUnidentifiedAccessCertificateRotationTime(context, nextTime);

    return nextTime;
  }

  public static void schedule(Context context) {
    new RotateSenderCertificateListener().onReceive(context, getScheduleIntent());
  }

}
