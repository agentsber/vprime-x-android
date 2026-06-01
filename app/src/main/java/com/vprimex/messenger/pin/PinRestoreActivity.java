package com.vprimex.messenger.pin;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.vprimex.messenger.MainActivity;
import com.vprimex.messenger.PassphraseRequiredActivity;
import com.vprimex.messenger.R;
import com.vprimex.messenger.lock.v2.CreateSvrPinActivity;
import com.vprimex.messenger.util.DynamicNoActionBarTheme;
import com.vprimex.messenger.util.DynamicTheme;

public final class PinRestoreActivity extends AppCompatActivity {

  private final DynamicTheme dynamicTheme = new DynamicNoActionBarTheme();

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    dynamicTheme.onCreate(this);
    super.onCreate(savedInstanceState);
    setContentView(R.layout.pin_restore_activity);
  }

  @Override
  protected void onResume() {
    super.onResume();
    dynamicTheme.onResume(this);
  }

  void navigateToPinCreation() {
    final Intent main      = MainActivity.clearTop(this);
    final Intent createPin = CreateSvrPinActivity.getIntentForPinCreate(this);
    final Intent chained   = PassphraseRequiredActivity.chainIntent(createPin, main);

    startActivity(chained);
    finish();
  }
}
