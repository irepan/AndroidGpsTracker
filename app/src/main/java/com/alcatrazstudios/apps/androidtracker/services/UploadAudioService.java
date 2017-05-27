package com.alcatrazstudios.apps.androidtracker.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.alcatrazstudios.apps.androidtracker.application.GpsTrackerApplication;
import com.alcatrazstudios.apps.androidtracker.model.CallRecordings;
import com.alcatrazstudios.apps.androidtracker.receiver.GpsTrackerConnectivityReceiver;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;

public class UploadAudioService extends Service {

    private static final String TAG="UploadCallsService";
    private boolean currentlyProcessingCallUpload=false;
    private Realm realm;
    private CallRecordings callRecordings;

    public UploadAudioService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        realm = Realm.getDefaultInstance();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {return null;}


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (!currentlyProcessingCallUpload){
            currentlyProcessingCallUpload=true;
            try {
                deleteOldFiles();
                if (GpsTrackerConnectivityReceiver.isConnected()) {
                    getCallDetails();
                }
            } catch (Throwable ierr){
                Log.e(TAG,"Error found" + ierr.getMessage());
            } finally {
                stopSelf();
            }
        }
        return START_NOT_STICKY;
    }

    private void getCallDetails(){
        RealmResults<CallRecordings> results;
        results = realm.where(CallRecordings.class).equalTo("setToUpload",true).findAllSorted("start");
        if (!results.isEmpty()){
            Iterator<CallRecordings> iterator = results.iterator();
            while (iterator.hasNext() && GpsTrackerConnectivityReceiver.isConnected()) {
                callRecordings = iterator.next();
                sendDataToWeb();
            }
        }
    }
    private void sendDataToWeb(){
        File file = new File(callRecordings.getFilePath(),callRecordings.getFileName());
        if (file.exists() && GpsTrackerConnectivityReceiver.isConnected()){
            doFileUpload(file);
        } else if (GpsTrackerConnectivityReceiver.isConnected()){
            return;
        } else {
            realm.beginTransaction();
            callRecordings.deleteFromRealm();
            realm.commitTransaction();
        }
    }

    private void doFileUpload(File selectedFile){
        Intent uploadIntent=new Intent(this,BackgroundUploadService.class);
        uploadIntent.putExtra("fname",selectedFile.getName());
        uploadIntent.putExtra("lname",selectedFile.getName());
        uploadIntent.putExtra("phone_no","");
        uploadIntent.putExtra("selectedFilePath",selectedFile.getAbsolutePath());
        startService(uploadIntent);
    }

    private void deleteOldFiles(){
        Date oldDate = new Date();
        Calendar calendar = Calendar.getInstance();
        int daysHistory= GpsTrackerApplication.getInstance().getConfiguration().getCallHistoryLimitDays();
        calendar.setTime(new Date());
        calendar.add(Calendar.DATE,daysHistory*-1);
        oldDate = calendar.getTime();
        realm.beginTransaction();
        RealmQuery<CallRecordings> oldFiles = realm.where(CallRecordings.class).equalTo("setToUpload",false).lessThanOrEqualTo("start",oldDate);
        RealmResults<CallRecordings> results= oldFiles.or().equalTo("uploaded",true).findAll();
        if (!results.isEmpty()) {
            Iterator<CallRecordings> iterator=results.iterator();
            while(iterator.hasNext()){
                CallRecordings callRecording=iterator.next();
                File file = new File(callRecording.getFilePath(),callRecording.getFileName());
                if (file.exists() && file.canWrite()){
                    file.delete();
                }
            }
            results.deleteAllFromRealm();
        }
        realm.commitTransaction();
    }
}
