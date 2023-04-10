package com.zoontek.rnbootsplash;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager.LayoutParams;

import androidx.annotation.NonNull;
import androidx.annotation.StyleRes;

public class RNBootSplashDialog extends Dialog {

  public RNBootSplashDialog(@NonNull Context context, @StyleRes int themeResId) {
    super(context, themeResId);
    setCancelable(false);
    setCanceledOnTouchOutside(false);
  }

  @Override
  public void onBackPressed() {
    // Prevent default behavior
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    final Window window = getWindow();

    if (window != null) {
      window.setLayout(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
      window.setWindowAnimations(R.style.Theme_BootSplashDialogNoAnimation);
    }

    super.onCreate(savedInstanceState);
  }

  public void setWindowAnimations(@StyleRes int resId) {
    final Window window = getWindow();

    if (window != null) {
      window.setWindowAnimations(resId);
    }
  }
}
