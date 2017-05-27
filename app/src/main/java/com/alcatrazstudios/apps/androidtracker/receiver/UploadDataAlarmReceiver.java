package com.alcatrazstudios.apps.androidtracker.receiver;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import com.alcatrazstudios.apps.androidtracker.services.SendDataService;

public class UploadDataAlarmReceiver extends WakefulBroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, SendDataService.class));
    }
}
