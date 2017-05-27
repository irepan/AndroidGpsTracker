package com.alcatrazstudios.apps.androidtracker.services;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Base64;
import android.util.Log;

import com.alcatrazstudios.apps.androidtracker.Utilities.LoopjHttpClient;
import com.alcatrazstudios.apps.androidtracker.application.GpsTrackerApplication;
import com.alcatrazstudios.apps.androidtracker.model.GpsTrackerEvent;
import com.alcatrazstudios.apps.androidtracker.receiver.GpsTrackerConnectivityReceiver;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.io.UnsupportedEncodingException;

import io.realm.Realm;
import io.realm.RealmResults;

public class SendDataService extends Service {
    private static final String TAG = "SendDataService";
    private boolean currentlySendingData=false;
    private Realm realm;
    private String phoneNo;
    private String googleAcct;
    private String deviceId;
    private String uploadWebsite;
    private GpsTrackerEvent event;

    public SendDataService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        realm = Realm.getDefaultInstance();
        SharedPreferences sharedPreferences = GpsTrackerApplication.getSharedPreferences();
        phoneNo = sharedPreferences.getString("phoneNo", "");
        googleAcct = sharedPreferences.getString("googleAcct", "");
        deviceId = sharedPreferences.getString("deviceId", "");
        uploadWebsite = sharedPreferences.getString("defaultUploadWebsite", "http://oragps.com/recibir/datos.php");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!currentlySendingData) {
            currentlySendingData=true;
            Log.d(TAG, "SendData");
            clearSentData();
            try {
                boolean isConnected=GpsTrackerConnectivityReceiver.isConnected();
                if (isConnected) {
                    sendData();
                } else {
                    stopSelf();
                }
            } catch (Throwable thrError) {
                thrError.printStackTrace();
                Log.e(TAG,"Error sending data OnStartCommand:" + thrError.getMessage(),thrError);
                stopSelf();
            }
        }
        return START_NOT_STICKY;
    }

    private void sendData() {
        RealmResults<GpsTrackerEvent> eventsResult;
        try {
            eventsResult = realm.where(GpsTrackerEvent.class).equalTo("uploaded", false).findAllSorted("id");
            if (!eventsResult.isEmpty()) {
                event = eventsResult.first();
                sendDataToWeb();
            } else {
                stopSelf();
            }
        } catch (Throwable thrError){
            throw thrError;
        }
    }

    private void sendDataToWeb() {
        String type = String.format("%d", event.getType());
        if (GpsTrackerConnectivityReceiver.isConnected()) {
            final RequestParams requestParams = new RequestParams();
            try {
                byte[] data = event.getPayLoad().getBytes("UTF-8");
                String payload = Base64.encodeToString(data, Base64.URL_SAFE | Base64.NO_WRAP);
                Log.d(TAG,payload);
                Log.d(TAG,event.getPayLoad());
                requestParams.add("googleAcct", googleAcct);
                requestParams.add("deviceId", deviceId);
                requestParams.add("phoneNo", phoneNo);
                requestParams.add("type", type);
                requestParams.add("payLoad", payload);
                LoopjHttpClient.post(uploadWebsite, requestParams, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody) {
                        try {
                            LoopjHttpClient.debugLoopJ(TAG, "sendLocationDataToWebsite - success", uploadWebsite, requestParams, responseBody, headers, statusCode, null);
                            Log.d(TAG, String.format("eventId=%d eventType=%d", event.getId(),event.getType()));
                            if (!realm.isInTransaction()) {
                                realm.beginTransaction();
                            }
                            event.deleteFromRealm();
                            realm.commitTransaction();
                        } catch (Throwable thrError) {
                            thrError.printStackTrace();
                            Log.e(TAG,"Error sending data OnSuccess:" + thrError.getMessage(),thrError);
                        } finally {
                            stopSelf();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] errorResponse, Throwable e) {
                        try {
                        LoopjHttpClient.debugLoopJ(TAG, "sendLocationDataToWebsite - failure", uploadWebsite, requestParams, errorResponse, headers, statusCode, e);
                        } catch (Throwable thrError) {
                            thrError.printStackTrace();
                            Log.e(TAG,"Error sending data OnSuccess:" + thrError.getMessage(),thrError);
                        } finally {
                            stopSelf();
                        }
                    }
                });
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                Log.e(TAG, "UnsupportedEncodingException: " + e.getMessage() ,e);
                stopSelf();
            }  catch (Throwable thrError) {
                thrError.printStackTrace();
                Log.e(TAG, "Error sending data post:" + thrError.getMessage(), thrError);
                stopSelf();
            }
        }

    }

    private void clearSentData() {
        try {
            if (!realm.isInTransaction()) {
                realm.beginTransaction();
            }
            RealmResults<GpsTrackerEvent> eventsResult;
            eventsResult = realm.where(GpsTrackerEvent.class).equalTo("uploaded", true).findAll();
            eventsResult.deleteAllFromRealm();
            realm.commitTransaction();
        } catch (Throwable thrError) {
            thrError.printStackTrace();
            Log.e(TAG,"Error clearSentData: " + thrError.getMessage(),thrError);
        }
    }
}
