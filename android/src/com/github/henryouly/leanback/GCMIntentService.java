package com.github.henryouly.leanback;

import com.google.android.gcm.GCMBaseIntentService;
import com.google.android.gcm.GCMRegistrar;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.github.henryouly.leanback.util.ServerUtilities;

public class GCMIntentService extends GCMBaseIntentService {

public static final String SENDER_ID = "527020451304";
  public GCMIntentService() {
    super(SENDER_ID);
}

@Override
protected void onRegistered(Context context, String registrationId) {
    Log.i(TAG, "Device registered: regId = " + registrationId);
    ServerUtilities.register(context, registrationId);
}

@Override
protected void onUnregistered(Context context, String registrationId) {
    Log.i(TAG, "Device unregistered");
    if (GCMRegistrar.isRegisteredOnServer(context)) {
        ServerUtilities.unregister(context, registrationId);
    } else {
        // This callback results from the call to unregister made on
        // ServerUtilities when the registration to the server failed.
        Log.i(TAG, "Ignoring unregister callback");
    }
}

@Override
protected void onMessage(Context context, Intent intent) {
    Log.i(TAG, "Received message");
}

@Override
protected void onDeletedMessages(Context context, int total) {
    Log.i(TAG, "Received deleted messages notification");
}

@Override
public void onError(Context context, String errorId) {
    Log.i(TAG, "Received error: " + errorId);
}

@Override
protected boolean onRecoverableError(Context context, String errorId) {
    // log message
    Log.i(TAG, "Received recoverable error: " + errorId);
    return super.onRecoverableError(context, errorId);
}

}
