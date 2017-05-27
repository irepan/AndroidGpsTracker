package com.alcatrazstudios.apps.androidtracker.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import com.alcatrazstudios.apps.androidtracker.services.TService;

public class CallRecordsReceiver extends WakefulBroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent intentNew = new Intent(context, TService.class);
        startWakefulService(context,intentNew);
    }
}
