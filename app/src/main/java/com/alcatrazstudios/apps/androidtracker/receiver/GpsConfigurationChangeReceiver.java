package com.alcatrazstudios.apps.androidtracker.receiver;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import com.alcatrazstudios.apps.androidtracker.services.GpsConfigurationChangerService;

public class GpsConfigurationChangeReceiver extends WakefulBroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, GpsConfigurationChangerService.class));
    }
}
