package com.zoontek.rnbootsplash;

import android.app.Activity;

import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;

public class RNBootSplash {

  public static void init(@Nullable final Activity activity,
                          @StyleRes final int bootThemeResId) {
    RNBootSplashModule.init(activity, bootThemeResId);
  }
}
