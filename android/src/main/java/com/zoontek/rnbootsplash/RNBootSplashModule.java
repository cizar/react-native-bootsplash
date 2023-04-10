package com.zoontek.rnbootsplash;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.window.SplashScreen;
import android.window.SplashScreenView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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

  private static final RNBootSplashQueue<Promise> mPromiseQueue = new RNBootSplashQueue<>();
  private static boolean mSplashVisible = false;

  public RNBootSplashModule(ReactApplicationContext reactContext) {
    super(reactContext);
  }

  @Override
  public String getName() {
    return NAME;
  }

  protected static void init(@Nullable final Activity activity) {
    if (activity == null) {
      FLog.w(
        ReactConstants.TAG,
        NAME + ": Ignored initialization, current activity is null.");
      return;
    }

    mSplashVisible = true;

    TypedValue typedValue = new TypedValue();
    Resources.Theme currentTheme = activity.getTheme();

    int backgroundResId = 0;
    @Nullable Drawable icon = null;

    if (currentTheme
      .resolveAttribute(R.attr.windowSplashScreenBackground, typedValue, true)) {
      backgroundResId = typedValue.resourceId;
    }

    if (currentTheme
      .resolveAttribute(R.attr.windowSplashScreenAnimatedIcon, typedValue, true)) {
      icon = currentTheme.getDrawable(typedValue.resourceId);
    }

    // Apply postSplashScreenTheme
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

    final int finalBackgroundResId = backgroundResId;
    @Nullable final Drawable finalIcon = icon;

    UiThreadUtil.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        View splashScreenView = FrameLayout.inflate(
          activity,
          R.layout.splash_screen_view,
          null
        );

        splashScreenView.setId(R.id.splash_screen_view);

        if (finalBackgroundResId != 0) {
          splashScreenView.setBackgroundResource(finalBackgroundResId);
        }

        if (finalIcon != null) {
          ImageView iconView = splashScreenView.findViewById(R.id.splashscreen_icon_view);
          iconView.setImageDrawable(finalIcon);
        }

        ViewGroup rootView = (ViewGroup) contentView.getRootView();
        rootView.addView(splashScreenView);

        shouldKeepOnScreen[0] = false;
      }
    });
  }

  private void clearPromiseQueue() {
    mSplashVisible = false;

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
    }, 100);
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

        final View view = activity.findViewById(R.id.splash_screen_view);

        if (view == null) {
          clearPromiseQueue();
          return;
        }

        final ViewGroup parent = (ViewGroup) view.getParent();

        if (fade) {
          view
            .animate()
            .alpha(0.0f)
            .setDuration(250)
            .setListener(new AnimatorListenerAdapter() {
              @Override
              public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

                if (parent != null) {
                  parent.removeView(view);
                }

                clearPromiseQueue();
              }
            })
            .setInterpolator(new AccelerateInterpolator())
            .start();
        } else {
          if (parent != null) {
            parent.removeView(view);
          }

          clearPromiseQueue();
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
    promise.resolve(mSplashVisible ? "visible" : "hidden");
  }
}
