package com.alcatrazstudios.apps.androidtracker.receiver;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import com.alcatrazstudios.apps.androidtracker.services.GpsConfigurationService;

public class GpsConfigurationReceiver extends WakefulBroadcastReceiver {

    public GpsConfigurationReceiver(){super();}
    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, GpsConfigurationService.class));
    }
}
