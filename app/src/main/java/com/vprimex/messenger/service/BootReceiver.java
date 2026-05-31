package com.vprimex.messenger.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.vprimex.messenger.dependencies.AppDependencies;
import com.vprimex.messenger.jobs.MessageFetchJob;

public class BootReceiver extends BroadcastReceiver {

  @Override
  public void onReceive(Context context, Intent intent) {
    AppDependencies.getJobManager().add(new MessageFetchJob());
  }
}
