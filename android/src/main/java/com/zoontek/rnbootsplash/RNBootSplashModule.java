package com.zoontek.rnbootsplash;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Build;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewTreeObserver;
import android.window.SplashScreen;
import android.window.SplashScreenView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;

import com.facebook.common.logging.FLog;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.UiThreadUtil;
import com.facebook.react.common.ReactConstants;
import com.facebook.react.module.annotations.ReactModule;

import java.util.Timer;
import java.util.TimerTask;

@ReactModule(name = RNBootSplashModule.NAME)
public class RNBootSplashModule extends ReactContextBaseJavaModule {

  public static final String NAME = "RNBootSplash";

  @Nullable
  private static RNBootSplashDialog mDialog = null;

  private static final RNBootSplashQueue<Promise> mPromiseQueue = new RNBootSplashQueue<>();
  private static boolean mShouldKeepOnScreen = true;

  public RNBootSplashModule(ReactApplicationContext reactContext) {
    super(reactContext);
  }

  @Override
  public String getName() {
    return NAME;
  }

  protected static void init(@Nullable final Activity activity,
                             @StyleRes final int bootThemeResId) {
    if (activity == null) {
      FLog.w(
        ReactConstants.TAG,
        NAME + ": Ignored initialization, current activity is null.");
      return;
    }

    // Apply postSplashScreenTheme
    TypedValue typedValue = new TypedValue();
    Resources.Theme currentTheme = activity.getTheme();

    if (currentTheme
      .resolveAttribute(R.attr.postSplashScreenTheme, typedValue, true)) {
      int finalThemeId = typedValue.resourceId;

      if (finalThemeId != 0) {
        activity.setTheme(finalThemeId);
      }
    }

    // Keep the splash screen on-screen until Dialog is shown
    final View contentView = activity.findViewById(android.R.id.content);

    contentView
      .getViewTreeObserver()
      .addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
        @Override
        public boolean onPreDraw() {
          if (mShouldKeepOnScreen) {
            return false;
          }

          contentView
            .getViewTreeObserver()
            .removeOnPreDrawListener(this);

          return true;
        }
      });

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      // This is not called on Android 12 when activity is started using intent
      // (Android studio / CLI / notification / widget…)
      activity
        .getSplashScreen()
        .setOnExitAnimationListener(new SplashScreen.OnExitAnimationListener() {
          @Override
          public void onSplashScreenExit(@NonNull SplashScreenView view) {
            view.remove(); // Remove it immediately, without animation
          }
        });
    }

    UiThreadUtil.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        mDialog = new RNBootSplashDialog(activity, bootThemeResId);

        mDialog.setOnShowListener(new DialogInterface.OnShowListener() {
          @Override
          public void onShow(DialogInterface dialog) {
            mShouldKeepOnScreen = false;
          }
        });

        mDialog.show();
      }
    });
  }

  private void clearPromiseQueue() {
    while (!mPromiseQueue.isEmpty()) {
      Promise promise = mPromiseQueue.shift();

      if (promise != null)
        promise.resolve(true);
    }
  }

  private void waitAndHide(final boolean fade) {
    final Timer timer = new Timer();

    timer.schedule(new TimerTask() {
      @Override
      public void run() {
        timer.cancel();
        hideAndResolveAll(fade);
      }
    }, 250);
  }

  private void hideAndResolveAll(final boolean fade) {
    UiThreadUtil.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        final Activity activity = getReactApplicationContext().getCurrentActivity();

        if (activity == null || activity.isFinishing()) {
          waitAndHide(fade);
          return;
        }

        if (mDialog == null) {
          clearPromiseQueue();
          return;
        }

        mDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
          @Override
          public void onDismiss(DialogInterface dialog) {
            mDialog = null;
            clearPromiseQueue();
          }
        });

        mDialog.setWindowAnimations(
          fade
            ? R.style.Theme_BootSplashDialogFading
            : R.style.Theme_BootSplashDialogNoAnimation
        );

         mDialog.dismiss();
      }
    });
  }

  @ReactMethod
  public void hide(final boolean fade, final Promise promise) {
    mPromiseQueue.push(promise);
    hideAndResolveAll(fade);
  }

  @ReactMethod
  public void getVisibilityStatus(final Promise promise) {
    promise.resolve(mDialog != null ? "visible" : "hidden");
  }
}
