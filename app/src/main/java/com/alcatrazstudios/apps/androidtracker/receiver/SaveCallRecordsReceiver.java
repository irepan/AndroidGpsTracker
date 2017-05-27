package com.alcatrazstudios.apps.androidtracker.receiver;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import com.alcatrazstudios.apps.androidtracker.services.CallRecordingsSaveService;

public class SaveCallRecordsReceiver extends WakefulBroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
//        context.startService(new Intent(context, CallRecordingsSaveService.class));
        startWakefulService(context,new Intent(context, CallRecordingsSaveService.class));
    }
}
