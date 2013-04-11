package com.github.henryouly.leanback;

import com.google.android.gcm.GCMRegistrar;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.github.henryouly.leanback.util.ServerUtilities;

public class LeanbackService extends Service {
  private static final String TAG = LeanbackService.class.getSimpleName();

  private final IBinder mBinder = new MyBinder();

  AsyncTask<Void, Void, Void> mRegisterTask;
  
  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    // Make sure the device has the proper dependencies.
    GCMRegistrar.checkDevice(this);
    // Make sure the manifest was properly set - comment out this line
    // while developing the app, then uncomment it when it's ready.
    GCMRegistrar.checkManifest(this);

    final String regId = GCMRegistrar.getRegistrationId(this);
    if (regId.equals("")) {
        // Automatically registers application on startup.
        GCMRegistrar.register(this, Constants.SENDER_ID);
    } else {
        // Device is already registered on GCM, check server.
        //if (GCMRegistrar.isRegisteredOnServer(this)) {
            // Skips registration.
        //} else {
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
        //}
    }
    
    Log.d(TAG, "onStartCommand");
    return Service.START_NOT_STICKY;
  }
   
  @Override
  public IBinder onBind(Intent arg0) {
    return mBinder;
  }
  
  @Override
  public void onDestroy() {
    Log.d(TAG, "onDestroy");
    if (mRegisterTask != null) {
      mRegisterTask.cancel(true);
    }
    GCMRegistrar.onDestroy(this);
    super.onDestroy();
  }

  public class MyBinder extends Binder {
    LeanbackService getService() {
      return LeanbackService.this;
    }
  }
}
