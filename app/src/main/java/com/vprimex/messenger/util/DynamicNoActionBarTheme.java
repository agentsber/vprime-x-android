package com.vprimex.messenger.util;

import androidx.annotation.StyleRes;

import com.vprimex.messenger.R;

public class DynamicNoActionBarTheme extends DynamicTheme {

  protected @StyleRes int getTheme() {
    return R.style.Signal_DayNight_NoActionBar;
  }
}
