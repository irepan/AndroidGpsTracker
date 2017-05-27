package com.alcatrazstudios.apps.androidtracker.services;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;

import com.alcatrazstudios.apps.androidtracker.application.GpsTrackerApplication;
import com.alcatrazstudios.apps.androidtracker.config.TrackerConfiguration;

public class GpsConfigurationChangerService extends Service {
    private static final String TAG="ConfigurationChanger";
    private boolean currentlyProcessingConfigChanges=false;
    public GpsConfigurationChangerService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!currentlyProcessingConfigChanges) {
            try {
                currentlyProcessingConfigChanges = true;
                Log.d(TAG, "Start looking for configuration differences");
                TrackerConfiguration configuration = GpsTrackerApplication.getInstance().getConfiguration();
                SharedPreferences sharedPreferences = GpsTrackerApplication.getSharedPreferences();
                if (!configuration.equals(sharedPreferences)) {
                    Log.d(TAG, "There are configuration differences");
                    boolean intervalChanged = configuration.isIntervalChanged(sharedPreferences);
//                    boolean callRecordChanged = configuration.isRecordCallsChanged(sharedPreferences);
                    configuration.setPreferences(sharedPreferences);

/*                    if (callRecordChanged) {
                        Log.d(TAG, "callRecordingChanged " + configuration.isRecordCalls());
                        GpsTrackerApplication.switchCallRecording(this);
                    } */

                    if (intervalChanged) {
                        Log.d(TAG, "Interval was changed");
                        GpsTrackerApplication.restartServices(this);
                    }

                }
            } catch (Throwable thrError) {
                thrError.printStackTrace();
                Log.e(TAG,"Error on Configuration Service" + thrError.getMessage(),thrError);
            } finally {
                stopSelf();
            }
        }
        return START_STICKY;
    }
}
