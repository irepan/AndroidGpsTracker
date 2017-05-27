package com.alcatrazstudios.apps.androidtracker.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.alcatrazstudios.apps.androidtracker.application.GpsTrackerApplication;

public class GpsTrackerBootReceiver extends BroadcastReceiver {
    private static final String TAG = "GpsTrackerBootReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e(TAG,"Starting all services");
        GpsTrackerApplication.startAlarmManager(context);
    }
}
