package com.vprimex.messenger.lock.v2;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.NavGraph;
import androidx.navigation.Navigation;

import com.vprimex.messenger.PassphrasePromptActivity;
import com.vprimex.messenger.PassphraseRequiredActivity;
import com.vprimex.messenger.R;
import com.vprimex.messenger.service.KeyCachingService;
import com.vprimex.messenger.util.DynamicRegistrationTheme;
import com.vprimex.messenger.util.DynamicTheme;

public class CreateSvrPinActivity extends PassphraseRequiredActivity {

  public static final int REQUEST_NEW_PIN = 27698;

  private final DynamicTheme dynamicTheme = new DynamicRegistrationTheme();

  public static @NonNull Intent getIntentForPinCreate(@NonNull Context context) {
    CreateSvrPinFragmentArgs args = new CreateSvrPinFragmentArgs.Builder()
                                                                .setIsForgotPin(false)
                                                                .setIsPinChange(false)
                                                                .build();

    return getIntentWithArgs(context, args);
  }

  public static @NonNull Intent getIntentForPinChangeFromForgotPin(@NonNull Context context) {
    CreateSvrPinFragmentArgs args = new CreateSvrPinFragmentArgs.Builder()
                                                                .setIsForgotPin(true)
                                                                .setIsPinChange(true)
                                                                .build();

    return getIntentWithArgs(context, args);
  }

  public static @NonNull Intent getIntentForPinChangeFromSettings(@NonNull Context context) {
    CreateSvrPinFragmentArgs args = new CreateSvrPinFragmentArgs.Builder()
                                                                .setIsForgotPin(false)
                                                                .setIsPinChange(true)
                                                                .build();

    return getIntentWithArgs(context, args);
  }

  private static @NonNull Intent getIntentWithArgs(@NonNull Context context, @NonNull CreateSvrPinFragmentArgs args) {
    return new Intent(context, CreateSvrPinActivity.class).putExtras(args.toBundle());
  }

  @Override
  public void onCreate(Bundle bundle, boolean ready) {
    super.onCreate(bundle, ready);

    if (KeyCachingService.isLocked(this)) {
      startActivity(getPromptPassphraseIntent());
      finish();
      return;
    }

    dynamicTheme.onCreate(this);

    setContentView(R.layout.create_kbs_pin_activity);

    CreateSvrPinFragmentArgs arguments = CreateSvrPinFragmentArgs.fromBundle(getIntent().getExtras());

    NavGraph graph = Navigation.findNavController(this, R.id.nav_host_fragment).getGraph();
    Navigation.findNavController(this, R.id.nav_host_fragment).setGraph(graph, arguments.toBundle());
  }

  @Override
  public void onResume() {
    super.onResume();
    dynamicTheme.onResume(this);
  }

  private Intent getPromptPassphraseIntent() {
    return getRoutedIntent(PassphrasePromptActivity.class, getIntent());
  }

  private Intent getRoutedIntent(Class<?> destination, @Nullable Intent nextIntent) {
    final Intent intent = new Intent(this, destination);
    if (nextIntent != null)   intent.putExtra("next_intent", nextIntent);
    return intent;
  }
}
