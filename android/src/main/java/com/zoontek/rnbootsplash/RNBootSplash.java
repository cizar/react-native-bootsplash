package com.zoontek.rnbootsplash;

import android.app.Activity;

import androidx.annotation.Nullable;

public class RNBootSplash {

  public static void init(@Nullable final Activity activity) {
    RNBootSplashModule.init(activity);
  }
}
