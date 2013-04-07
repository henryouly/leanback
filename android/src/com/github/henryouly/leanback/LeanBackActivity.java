package com.github.henryouly.leanback;

import com.google.android.gcm.GCMRegistrar;

import com.github.henryouly.leanback.util.ServerUtilities;
import com.github.henryouly.leanback.util.SystemUiHider;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;

/**
 * An example full-screen activity that shows and hides the system UI (i.e. status bar and
 * navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class LeanBackActivity extends Activity {
  /**
   * Whether or not the system UI should be auto-hidden after {@link #AUTO_HIDE_DELAY_MILLIS}
   * milliseconds.
   */
  private static final boolean AUTO_HIDE = true;

  /**
   * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after user interaction before
   * hiding the system UI.
   */
  private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

  /**
   * If set, will toggle the system UI visibility upon interaction. Otherwise, will show the system
   * UI visibility upon interaction.
   */
  private static final boolean TOGGLE_ON_CLICK = true;

  /**
   * The flags to pass to {@link SystemUiHider#getInstance}.
   */
  private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

  /**
   * The instance of the {@link SystemUiHider} for this activity.
   */
  private SystemUiHider mSystemUiHider;
  
  private GCMReceiver mHandleMessageReceiver;
  private IntentFilter mOnMessageFilter;


  AsyncTask<Void, Void, Void> mRegisterTask;
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    
    mHandleMessageReceiver = new GCMReceiver();
    mOnMessageFilter = new IntentFilter();
    mOnMessageFilter.addAction(Constants.ACTION_ON_MESSAGE);

    // Make sure the device has the proper dependencies.
    GCMRegistrar.checkDevice(this);
    // Make sure the manifest was properly set - comment out this line
    // while developing the app, then uncomment it when it's ready.
    GCMRegistrar.checkManifest(this);
    
    setContentView(R.layout.activity_lean_back);

    final View controlsView = findViewById(R.id.fullscreen_content_controls);
    final View contentView = findViewById(R.id.fullscreen_content);

    // Set up an instance of SystemUiHider to control the system UI for
    // this activity.
    mSystemUiHider = SystemUiHider.getInstance(this, contentView, HIDER_FLAGS);
    mSystemUiHider.setup();
    mSystemUiHider.setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
      // Cached values.
      int mControlsHeight;
      int mShortAnimTime;

      @Override
      @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
      public void onVisibilityChange(boolean visible) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
          // If the ViewPropertyAnimator API is available
          // (Honeycomb MR2 and later), use it to animate the
          // in-layout UI controls at the bottom of the
          // screen.
          if (mControlsHeight == 0) {
            mControlsHeight = controlsView.getHeight();
          }
          if (mShortAnimTime == 0) {
            mShortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
          }
          controlsView.animate()
              .translationY(visible ? 0 : mControlsHeight).setDuration(mShortAnimTime);
        } else {
          // If the ViewPropertyAnimator APIs aren't
          // available, simply show or hide the in-layout UI
          // controls.
          controlsView.setVisibility(visible ? View.VISIBLE : View.GONE);
        }

        if (visible && AUTO_HIDE) {
          // Schedule a hide().
          delayedHide(AUTO_HIDE_DELAY_MILLIS);
        }
      }
      
      
    });

    // Set up the user interaction to manually show or hide the system UI.
    contentView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        if (TOGGLE_ON_CLICK) {
          mSystemUiHider.toggle();
        } else {
          mSystemUiHider.show();
        }
      }
    });

    // Upon interacting with UI controls, delay any scheduled hide()
    // operations to prevent the jarring behavior of controls going away
    // while interacting with the UI.
    findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);
    findViewById(R.id.dummy_button).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        //String dataUri = "http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4";
        //MediaPlayer.streamFrom(this, dataUri);
        finish();
      }
    });
    
    final String regId = GCMRegistrar.getRegistrationId(this);
    if (regId.equals("")) {
        // Automatically registers application on startup.
        GCMRegistrar.register(this, Constants.SENDER_ID);
    } else {
        // Device is already registered on GCM, check server.
        if (GCMRegistrar.isRegisteredOnServer(this)) {
            // Skips registration.
        } else {
            // Try to register again, but not in the UI thread.
            // It's also necessary to cancel the thread onDestroy(),
            // hence the use of AsyncTask instead of a raw thread.
            final Context context = this;
            mRegisterTask = new AsyncTask<Void, Void, Void>() {

                @Override
                protected Void doInBackground(Void... params) {
                    boolean registered =
                            ServerUtilities.register(context, regId);
                    // At this point all attempts to register with the app
                    // server failed, so we need to unregister the device
                    // from GCM - the app will try to register again when
                    // it is restarted. Note that GCM will send an
                    // unregistered callback upon completion, but
                    // GCMIntentService.onUnregistered() will ignore it.
                    if (!registered) {
                        GCMRegistrar.unregister(context);
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Void result) {
                    mRegisterTask = null;
                }

            };
            mRegisterTask.execute(null, null, null);
        }
    }
    
    registerReceiver(mHandleMessageReceiver, mOnMessageFilter);
  }

  @Override
  protected void onPostCreate(Bundle savedInstanceState) {
    super.onPostCreate(savedInstanceState);

    // Trigger the initial hide() shortly after the activity has been
    // created, to briefly hint to the user that UI controls
    // are available.
    delayedHide(100);
  }


  @Override
  protected void onDestroy() {
      if (mRegisterTask != null) {
          mRegisterTask.cancel(true);
      }
      unregisterReceiver(mHandleMessageReceiver);
      GCMRegistrar.onDestroy(this);
      super.onDestroy();
  }
  
  /**
   * Touch listener to use for in-layout UI controls to delay hiding the system UI. This is to
   * prevent the jarring behavior of controls going away while interacting with activity UI.
   */
  View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
      if (AUTO_HIDE) {
        delayedHide(AUTO_HIDE_DELAY_MILLIS);
      }
      return false;
    }
  };

  Handler mHideHandler = new Handler();
  Runnable mHideRunnable = new Runnable() {
    @Override
    public void run() {
      mSystemUiHider.hide();
    }
  };

  /**
   * Schedules a call to hide() in [delay] milliseconds, canceling any previously scheduled calls.
   */
  private void delayedHide(int delayMillis) {
    mHideHandler.removeCallbacks(mHideRunnable);
    mHideHandler.postDelayed(mHideRunnable, delayMillis);
  }
  
  private class GCMReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
      String dataUri = intent.getStringExtra(Constants.FIELD_MESSAGE);
      String packageName = "com.mxtech.videoplayer.ad";
      String intentName = "android.intent.action.VIEW";
      PackageManager packageManager = getPackageManager();
      Intent launchIntent;
      if (intentName.length() > 0) {
        launchIntent = new Intent(intentName);
      } else {
        launchIntent = packageManager.getLaunchIntentForPackage(packageName);
      }
      Uri uri = Uri.parse(dataUri);
      String dataType = "video/*";
      if (dataType.length() > 0) {
        launchIntent.setDataAndType(uri, dataType);          
      } else {
        launchIntent.setData(uri);
      }
      launchIntent.setPackage(packageName);
      startActivity(launchIntent);
    }
  }
}
