package com.alcatrazstudios.apps.androidtracker.services;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;

import com.alcatrazstudios.apps.androidtracker.Utilities.LoopjHttpClient;
import com.alcatrazstudios.apps.androidtracker.application.GpsTrackerApplication;
import com.alcatrazstudios.apps.androidtracker.config.TrackerConfiguration;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;

public class GpsConfigurationService extends Service {
    private boolean currentlyProcessingConfig=false;
    private static final String TAG="GpsConfigurationService";
    private String phoneNo;
    private String googleAcct;
    private String deviceId;
    private String configWebsite;

    private static RequestParams requestParams;

    public GpsConfigurationService() {super();}

    @Override
    public IBinder onBind(Intent intent) {return null;}

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!currentlyProcessingConfig) {
            currentlyProcessingConfig=true;
            try {
                requestParams = new RequestParams();
                SharedPreferences sharedPreferences = GpsTrackerApplication.getSharedPreferences();
                googleAcct=sharedPreferences.getString("googleAcct","");
                phoneNo=sharedPreferences.getString("phoneNo","");
                deviceId=sharedPreferences.getString("deviceId","");
                configWebsite = sharedPreferences.getString("defaultConfigWebsite", "http://oragps.com/recibir/config.php");
                requestParams.add("googleAcct",googleAcct);
                requestParams.add("deviceId",deviceId);
                requestParams.add("phoneNo",phoneNo);
                readConfiguration();
            } catch (Throwable thrError) {
                thrError.printStackTrace();
                Log.e(TAG,"Error GpsConfigurationService: " + thrError.getMessage(),thrError);
                stopSelf();
            }
        }
        return START_STICKY;
    }

    private void readConfiguration(){
        Log.d(TAG,"Starting load configuration from web");
//        final RequestParams requestParams = new RequestParams();

        LoopjHttpClient.post(configWebsite, requestParams, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody) {
                Log.d(TAG,"Configuratino received OK from web");
                try {
                    LoopjHttpClient.debugLoopJ(TAG, "defaultConfigWebsite - success", configWebsite, requestParams, responseBody, headers, statusCode, null);
                    String response=new String(responseBody).replace("(","").replace(")","").trim();
                    if (response.isEmpty()) {
                        JSONArray jArray = new JSONArray(response);
                        JSONObject responseObj = jArray.getJSONObject(0);
                        TrackerConfiguration configuration = new TrackerConfiguration(responseObj);
                        if (!configuration.equals(GpsTrackerApplication.getInstance().getConfiguration())) {
                            Log.d(TAG, "Configuration change received");
                            GpsTrackerApplication.getInstance().setConfiguration(configuration);
                        } else {
                            Log.e(TAG,"Configuration reads empty");
                        }
                        Log.d(TAG, "Response "+ response);
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
                    LoopjHttpClient.debugLoopJ(TAG, "defaultConfigWebsite - failure", configWebsite, requestParams, errorResponse, headers, statusCode, e);
                    Log.e(TAG, "There is a problem reading configuration");
                    Log.e(TAG,"Error : " + new String(errorResponse));
                } catch (Throwable thrError) {
                    thrError.printStackTrace();
                    Log.e(TAG, "Error Loop On Success: " + thrError.getMessage(), thrError);
                } finally {
                    stopSelf();
                }
            }
        });

    }
}
