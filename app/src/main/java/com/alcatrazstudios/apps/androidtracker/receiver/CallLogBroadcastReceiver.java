package com.alcatrazstudios.apps.androidtracker.receiver;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import com.alcatrazstudios.apps.androidtracker.services.CallLogService;

public class CallLogBroadcastReceiver  extends WakefulBroadcastReceiver {
    public CallLogBroadcastReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, CallLogService.class));
    }
}
