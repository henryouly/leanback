package com.github.henryouly.leanback;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;

public class LeanbackReceiver extends BroadcastReceiver {
  @Override
  public void onReceive(Context context, Intent intent) {
    String action = intent.getAction();
    if (action.equals(Constants.ACTION_ON_MESSAGE)) {
      String dataUri = intent.getStringExtra(Constants.FIELD_MESSAGE);
      String packageName = "com.mxtech.videoplayer.ad";
      String intentName = "android.intent.action.VIEW";
      PackageManager packageManager = context.getPackageManager();
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
      launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      context.startActivity(launchIntent);
    } else if (action.equals("android.intent.action.BOOT_COMPLETED")) {
      Intent service = new Intent(context, LeanbackService.class);
      context.startService(service);
    }
  }
}