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

  private enum Status {
    VISIBLE,
    HIDDEN,
    TRANSITIONING
  }

  @Nullable
  private static RNBootSplashDialog mDialog = null;

  private static final RNBootSplashQueue<Promise> mPromiseQueue = new RNBootSplashQueue<>();
  private static Status mStatus = Status.HIDDEN;

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

    mStatus = Status.VISIBLE;

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
    final boolean[] shouldKeepOnScreen = {true};

    contentView
      .getViewTreeObserver()
      .addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
        @Override
        public boolean onPreDraw() {
          if (shouldKeepOnScreen[0]) {
            return false;
          }

          contentView
            .getViewTreeObserver()
            .removeOnPreDrawListener(this);

          return true;
        }
      });

    // This is not called on Android 12 when activity is started using intent
    // (Android studio / CLI / notification / widget…)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      activity
        .getSplashScreen()
        .setOnExitAnimationListener(new SplashScreen.OnExitAnimationListener() {
          @Override
          public void onSplashScreenExit(@NonNull SplashScreenView view) {
            view.remove(); // Remove it immediately, without animation
          }
        });
    }

    mDialog = new RNBootSplashDialog(activity, bootThemeResId);

    mDialog.setOnShowListener(new DialogInterface.OnShowListener() {
      @Override
      public void onShow(DialogInterface dialog) {
        shouldKeepOnScreen[0] = false;
      }
    });

    UiThreadUtil.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        mDialog.setWindowAnimations(R.style.Theme_NoAnimBootSplashDialog);
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

  private void hideAndResolveAll(final boolean fade) {
    UiThreadUtil.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        final Activity activity = getReactApplicationContext().getCurrentActivity();

        if (activity == null || activity.isFinishing()) {
          // Wait for activity to be ready
          final Timer timer = new Timer();

          timer.schedule(new TimerTask() {
            @Override
            public void run() {
              timer.cancel();
              hideAndResolveAll(fade);
            }
          }, 250);
        } else if (mDialog == null || mStatus == Status.HIDDEN) {
          clearPromiseQueue();
        } else {
          if (fade) {
            mStatus = Status.TRANSITIONING;
            mDialog.setWindowAnimations(R.style.Theme_FadingBootSplashDialog);
          } else {
            mDialog.setWindowAnimations(R.style.Theme_NoAnimBootSplashDialog);
          }

          mDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
              mStatus = Status.HIDDEN;
              mDialog = null;
              clearPromiseQueue();
            }
          });

          mDialog.dismiss();
        }
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
    switch (mStatus) {
      case VISIBLE:
        promise.resolve("visible");
        break;
      case HIDDEN:
        promise.resolve("hidden");
        break;
      case TRANSITIONING:
        promise.resolve("transitioning");
        break;
    }
  }
}
