package com.alcatrazstudios.apps.androidtracker.services;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.IBinder;
import android.provider.CallLog;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.alcatrazstudios.apps.androidtracker.model.Call;
import com.alcatrazstudios.apps.androidtracker.model.GpsTrackerEvent;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.realm.Realm;

public class CallLogService extends Service {
    private static final String TAG="CallLogService";
    private boolean currentlyProcessingCallLog=false;
    private Realm realm;

    public CallLogService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        realm = Realm.getDefaultInstance();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!currentlyProcessingCallLog) {
            currentlyProcessingCallLog=true;
            try {
                String payload = getCallDetails();
                if (payload != null) {
                    insertEventToDB(3, payload);
                }
            }catch (Throwable thrError){
                thrError.printStackTrace();
                Log.e(TAG,"Error on CallLogService: " + thrError.getMessage(),thrError);
            } finally {
                stopSelf();
            }
        }
        return START_NOT_STICKY;
    }

    protected void insertEventToDB(int eventType, String payload) {
        realm.beginTransaction();
        GpsTrackerEvent trackerEvent = new GpsTrackerEvent(eventType, payload, new Date());
        realm.copyToRealm(trackerEvent);
        realm.commitTransaction();
    }

    private String getCallDetails() {
        Log.e(TAG,"Starting get call Logs");
        String result=null;
        Gson gson = new Gson();

        List<Call> calls = new ArrayList<>();

        String strOrder[] = {android.provider.CallLog.Calls.DATE + " DESC"};


        /* Query the CallLog Content Provider */
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return "{}";
        }
        Cursor managedCursor = getContentResolver().query(
                CallLog.Calls.CONTENT_URI, null, null, null, null);
        if (managedCursor != null && managedCursor.getColumnCount() > 0) {
            Log.e(TAG,"There are logs to read");
            int number = managedCursor.getColumnIndex(CallLog.Calls.NUMBER);
            int type = managedCursor.getColumnIndex(CallLog.Calls.TYPE);
            int date = managedCursor.getColumnIndex(CallLog.Calls.DATE);
            int duration = managedCursor.getColumnIndex(CallLog.Calls.DURATION);
            try {
                while (managedCursor.moveToNext()) {
                    String phNum = managedCursor.getString(number);
                    String callTypeCode = managedCursor.getString(type);
                    String strcallDate = managedCursor.getString(date);
                    Date callDate = new Date(Long.valueOf(strcallDate));
                    String callDuration = managedCursor.getString(duration);
                    String callType = null;
                    int callcode = Integer.parseInt(callTypeCode);
                    switch (callcode) {
                        case CallLog.Calls.OUTGOING_TYPE:
                            callType = "Outgoing";
                            break;
                        case CallLog.Calls.INCOMING_TYPE:
                            callType = "Incoming";
                            break;
                        case CallLog.Calls.MISSED_TYPE:
                            callType = "Missed";
                            break;
                    }
                    calls.add(new Call(callDate, phNum, callType, callDuration));

                }
                result=gson.toJson(calls);
                Log.e(TAG,"Saving call logs to DB");
            } catch(Throwable exception) {
                Log.e(TAG, "Error reading log" + exception.getMessage());
                exception.printStackTrace();
            }finally{
                managedCursor.close();
            }
        }
        return result;
    }

}
