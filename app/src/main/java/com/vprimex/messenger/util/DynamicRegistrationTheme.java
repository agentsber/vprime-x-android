package com.vprimex.messenger.util;

import androidx.annotation.StyleRes;

import com.vprimex.messenger.R;

public class DynamicRegistrationTheme extends DynamicTheme {

  protected @StyleRes int getTheme() {
    return R.style.Signal_DayNight_Registration;
  }
}
