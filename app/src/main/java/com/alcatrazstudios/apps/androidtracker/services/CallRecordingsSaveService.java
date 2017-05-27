package com.alcatrazstudios.apps.androidtracker.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.alcatrazstudios.apps.androidtracker.application.GpsTrackerApplication;
import com.alcatrazstudios.apps.androidtracker.model.CallRecordings;
import com.alcatrazstudios.apps.androidtracker.model.GpsTrackerEvent;
import com.alcatrazstudios.apps.androidtracker.receiver.GpsTrackerConnectivityReceiver;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Modifier;
import java.util.Date;
import java.util.Iterator;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class CallRecordingsSaveService extends Service {
    private static final String TAG="CallsRecSaveService";
    private boolean currentlyProcessingCallRecSave =false;
    private Realm realm;
    private CallRecordings callRecordings;

    public CallRecordingsSaveService() {
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

        if (!currentlyProcessingCallRecSave){
            currentlyProcessingCallRecSave =true;
            try {
                if (GpsTrackerConnectivityReceiver.isConnected()) {
                    publishLatestList();
                }
            } catch (Throwable ierr){
                Log.e(TAG,"Error found " + ierr.getMessage());
            } finally {
                stopSelf();
            }
        }
        return START_NOT_STICKY;
    }
    private void publishLatestList()  throws JSONException{
        String recordings = "[]";

        GsonBuilder gsonBuilder  = new GsonBuilder();
        // Allowing the serialization of static fields
        gsonBuilder.excludeFieldsWithModifiers(Modifier.STATIC);
        // Creates a Gson instance based on the current configuration
        Gson gson = gsonBuilder.create();
//        String json = gson.toJson(objectToSerialize);
//        System.out.println(json);

//        Gson gson = new Gson();
        Log.i(TAG,"Step 1");
        RealmResults<CallRecordings> results = realm.where(CallRecordings.class).equalTo("uploaded",false).equalTo("setToUpload",false).findAllSorted("start");
        if (!results.isEmpty()) {
//            JSONArray jsonArray = new JSONArray();

//            CallRecordings[] callRecordingses = results.toArray(new CallRecordings[results.size()]);


//            jsonArray.put(callRecordingses);
            recordings = "[";
            int index=0;
            Iterator<CallRecordings> iterator = results.iterator();
            while (iterator.hasNext()){
                CallRecordings callRecordings = iterator.next();
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("fileName",callRecordings.getFileName());
                jsonObject.put("filePath",callRecordings.getFilePath());
                jsonObject.put("number",callRecordings.getNumber());
                jsonObject.put("type",callRecordings.getType());
                jsonObject.put("start",callRecordings.getStart());
                jsonObject.put("end",callRecordings.getEnd());
                jsonObject.put("uploaded",callRecordings.isSetToUpload());
                jsonObject.put("setToUpload",callRecordings.isSetToUpload());

                if (index>0){
                    recordings += ",";
                }

                recordings += jsonObject.toString();
                index++;
//                jsonArray.put(iterator.next());
            }
            recordings += "]";
//            recordings=jsonArray.toString();
//            recordings=jsonArray.toString().substring(19);
//            recordings = recordings.replaceAll("\"]","");
//            recordings=gson.toJson(callRecordingses[0]);
            Log.i(TAG,"events: " +  recordings);


            Log.i(TAG,"Step 2");

            GpsTrackerEvent event = realm.where(GpsTrackerEvent.class).equalTo("type",4).findFirst() ;//.findAll().sort("date", Sort.DESCENDING).first();
            if (event != null) {
                realm.beginTransaction();
                event.setPayLoad(recordings);
                event.setDate(new Date());
            } else {
                event = new GpsTrackerEvent(4, recordings, new Date());
                realm.beginTransaction();
            }


            Log.i(TAG,"Step 3 " + gson.toJson(event));

            realm.copyToRealmOrUpdate(event);
        } else {
            GpsTrackerEvent event = realm.where(GpsTrackerEvent.class).equalTo("type",4).findFirst();
            if (event != null){
                realm.beginTransaction();
                event.deleteFromRealm();
            } else {
                return;
            }
        }

        realm.commitTransaction();
    }

    private void publishLatestList_old(){
        RealmResults<GpsTrackerEvent> events=realm.where(GpsTrackerEvent.class).equalTo("type",4).findAll();
        if (!events.isEmpty()) {
            Iterator<GpsTrackerEvent> iterator = events.iterator();
            realm.beginTransaction();
            while (iterator.hasNext()){
                iterator.next().setUploaded(true);
            }
//            realm.beginTransaction();
            realm.insertOrUpdate(events);
            realm.copyToRealmOrUpdate(events);
//            events.deleteAllFromRealm();
            realm.commitTransaction();
        }
/*        RealmResults<CallRecordings> results1 = realm.where(CallRecordings.class).findAll();
        if (!results1.isEmpty()){
            Log.d(TAG,"Calls" + results1.size());
            Iterator<CallRecordings> iterator = results1.iterator();
            while (iterator.hasNext()){
                CallRecordings recordings = iterator.next();
                Log.d(TAG,"record" + recordings.isSetToUpload());
            }
        }*/

        GpsTrackerEvent event = null;
        RealmResults<CallRecordings> results = realm.where(CallRecordings.class).equalTo("uploaded",false).equalTo("setToUpload",false).findAllSorted("start");
        if (!results.isEmpty()) {
            Gson gson = new Gson();
//            CallRecordings[] recordings = results.toArray(new CallRecordings[results.size()]);
            JSONArray jsonArray = new JSONArray(results);
//                    event = new GpsTrackerEvent(4, gson.toJson(recordings), new Date());
            Log.i(TAG,"events: " +  jsonArray.toString());
            event = new GpsTrackerEvent(4, jsonArray.toString(), new Date());
        } else {
            Gson gson = new Gson();
/*            CallRecordings[] recordings =  {new CallRecordings(CallRecordings.IN_CALL,new File("./Files/"+ UUID.randomUUID().toString()),"+523315424289",new Date(),new Date()),
            new CallRecordings(CallRecordings.OUT_CALL,new File(UUID.randomUUID().toString()),"+523316293049",new Date(), new Date())};*/
            CallRecordings[] recordings =  {};

            event = new GpsTrackerEvent(4, gson.toJson(recordings), new Date());
        }
        realm.beginTransaction();
        realm.copyToRealm(event);
        realm.commitTransaction();
    }
}
