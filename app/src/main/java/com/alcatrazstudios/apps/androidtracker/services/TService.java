package com.alcatrazstudios.apps.androidtracker.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.alcatrazstudios.apps.androidtracker.application.GpsTrackerApplication;
import com.alcatrazstudios.apps.androidtracker.config.TrackerConfiguration;
import com.alcatrazstudios.apps.androidtracker.model.CallRecordings;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.UUID;

import io.realm.Realm;

public class TService extends Service {

    private static MediaRecorder recorder;
    private static File audioFile;
    private static boolean recordStarted = false;
    private static boolean tServiceStarted = false;

    private static final String ACTION_IN = "android.intent.action.PHONE_STATE";
    private static final String ACTION_OUT = "android.intent.action.NEW_OUTGOING_CALL";

    private static final String TAG="TService";

    private static CallReceiver callReceiver=null;
    private Realm realm;


    public static final String IN_CALL="In";
    public static final String OUT_CALL="Out";


    @Override
    public IBinder onBind(Intent arg0) { return null; }

    @Override
    public void onDestroy() {
        Log.d("service", "destroy");
        this.unregisterReceiver(callReceiver);
        super.onDestroy();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG,"onCreate TService ");

        realm = Realm.getDefaultInstance();

        try {
            final IntentFilter filter = new IntentFilter();
            filter.addAction(ACTION_OUT);
            filter.addAction(ACTION_IN);
            callReceiver = new CallReceiver();
            this.registerReceiver(callReceiver, filter);
        } catch (Throwable thrError) {
            thrError.printStackTrace();
            Log.e(TAG,"Error found at onCreate: " + thrError.getMessage(),thrError);
            stopSelf();
        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        Log.d("StartService", "TService");
        if (tServiceStarted == false) {
            Log.d(TAG,"Start the service");
            tServiceStarted = true;
        }

        return START_NOT_STICKY;
    }

    private File obtainNewFile() {
        File result = null;
        try {
            File baseFolder = new File(this.getExternalFilesDir(null), "/AudioRecordings");
            if (!baseFolder.exists()) {
                baseFolder.mkdirs();
            }
            String fileName = UUID.randomUUID().toString() + ".amr";
            result = new File(baseFolder, fileName);
            result.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG,"Creating a file: " + e.getMessage(),e);
        }catch (IllegalStateException e) {
            e.printStackTrace();
            Log.e(TAG,"Creating a file: " + e.getMessage(),e);
        } catch (Throwable thrErrors) {
            thrErrors.printStackTrace();
            Log.e(TAG,"Creating a file: " + thrErrors.getMessage(),thrErrors);
            result = null;
        }
        return result;
    }

    private void startRecording() {
        TrackerConfiguration configuration = GpsTrackerApplication.getInstance().getConfiguration();
/*        File sampleDir = new File(this.getExternalFilesDir(null), "/TestRecordingData1");
        if (!sampleDir.exists()) {
            sampleDir.mkdirs();
        }
        String file_name = "Record";
        try {
            audioFile = File.createTempFile(file_name, ".amr", sampleDir);
        } catch (IOException e) {
            e.printStackTrace();
        }  */
        audioFile = obtainNewFile();

        if (audioFile == null) {
            return;
        }


        Log.i(TAG,"file Path " + audioFile.getAbsolutePath());
        try {
            recorder = new MediaRecorder();

            recorder.setAudioSource(configuration.getRecordingMediaType()==0?MediaRecorder.AudioSource.MIC:MediaRecorder.AudioSource.VOICE_COMMUNICATION);
    //        recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION);
    //        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            try {
                recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            } catch (IllegalStateException iee) {
                Log.i(TAG,"THREE_GPP not working on this phone, using AMR_NB instead");
                recorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
            }
    //        recorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            recorder.setAudioEncodingBitRate(16);
            recorder.setAudioSamplingRate(8000);
            recorder.setOutputFile(audioFile.getAbsolutePath());
            recorder.prepare();
            recorder.start();
            recordStarted = true;
        } catch (IllegalStateException e) {
            e.printStackTrace();
            Log.e(TAG,"Start recording: " + e.getMessage(),e);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG,"Start recording: " + e.getMessage(),e);
        } catch (Throwable thrError) {
            thrError.printStackTrace();
            Log.e(TAG,"Start recording: " + thrError.getMessage(),thrError);
        }
    }

    private void stopRecording(String type, String number, Date start, Date end) {
        if (recordStarted) {
            try {
                recorder.stop();
                recordStarted = false;
                realm.beginTransaction();
                CallRecordings callRecordings = new CallRecordings(type, audioFile, number, start, end);

                //comment this in the future
                //callRecordings.setSetToUpload(true);
                Gson gson = new Gson();
                String object = gson.toJson(callRecordings);
                realm.copyToRealm(callRecordings);
                realm.commitTransaction();
                Log.i(TAG,"Call recordef " + object);
            } catch (Throwable thrError) {
                thrError.printStackTrace();
                Log.e(TAG,"stopRecorging" + thrError.getMessage(),thrError);
                stopSelf();
            }
        }
    }


    public abstract class PhonecallReceiver extends BroadcastReceiver {

        //The receiver will be recreated whenever android feels like it.  We need a static variable to remember data between instantiations

        private int lastState = TelephonyManager.CALL_STATE_IDLE;
        private Date callStartTime;
        private boolean isIncoming;
        private String savedNumber;  //because the passed incoming is only valid in ringing


        @Override
        public void onReceive(Context context, Intent intent) {
            //We listen to two intents.  The new outgoing call only tells us of an outgoing call.  We use it to get the number.
            if (intent.getAction().equals("android.intent.action.NEW_OUTGOING_CALL")) {
                savedNumber = intent.getExtras().getString("android.intent.extra.PHONE_NUMBER");
            } else {
                String stateStr = intent.getExtras().getString(TelephonyManager.EXTRA_STATE);
                String number = intent.getExtras().getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
                int state = 0;
                if (stateStr.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                    state = TelephonyManager.CALL_STATE_IDLE;
                } else if (stateStr.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                    state = TelephonyManager.CALL_STATE_OFFHOOK;
                } else if (stateStr.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                    state = TelephonyManager.CALL_STATE_RINGING;
                }


                onCallStateChanged(context, state, number);
            }
        }

        //Derived classes should override these to respond to specific events of interest
        protected abstract void onIncomingCallReceived(Context ctx, String number, Date start);

        protected abstract void onIncomingCallAnswered(Context ctx, String number, Date start);

        protected abstract void onIncomingCallEnded(Context ctx, String number, Date start, Date end);

        protected abstract void onOutgoingCallStarted(Context ctx, String number, Date start);

        protected abstract void onOutgoingCallEnded(Context ctx, String number, Date start, Date end);

        protected abstract void onMissedCall(Context ctx, String number, Date start);

        //Deals with actual events

        //Incoming call-  goes from IDLE to RINGING when it rings, to OFFHOOK when it's answered, to IDLE when its hung up
        //Outgoing call-  goes from IDLE to OFFHOOK when it dials out, to IDLE when hung up
        public void onCallStateChanged(Context context, int state, String number) {
            TrackerConfiguration configuration = GpsTrackerApplication.getInstance().getConfiguration();

            if (lastState == state) {
                //No change, debounce extras
                return;
            }
            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING:
                    isIncoming = true;
                    callStartTime = new Date();
                    savedNumber = number;
                    onIncomingCallReceived(context, number, callStartTime);
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    //Transition of ringing->offhook are pickups of incoming calls.  Nothing done on them
                    if (lastState != TelephonyManager.CALL_STATE_RINGING) {
                        isIncoming = false;
                        callStartTime = new Date();
                        if (configuration.isRecordCalls()) {
                            startRecording();
                        }
                        onOutgoingCallStarted(context, savedNumber, callStartTime);
                    } else {
                        isIncoming = true;
                        callStartTime = new Date();
                        if (configuration.isRecordCalls()) {
                            startRecording();
                        }
                        onIncomingCallAnswered(context, savedNumber, callStartTime);
                    }

                    break;
                case TelephonyManager.CALL_STATE_IDLE:
                    //Went to idle-  this is the end of a call.  What type depends on previous state(s)
                    if (lastState == TelephonyManager.CALL_STATE_RINGING) {
                        //Ring but no pickup-  a miss
                        onMissedCall(context, savedNumber, callStartTime);
                    } else if (isIncoming) {
                        stopRecording(IN_CALL,savedNumber, callStartTime, new Date());
                        onIncomingCallEnded(context, savedNumber, callStartTime, new Date());
                    } else {
                        stopRecording(OUT_CALL,savedNumber, callStartTime, new Date());
                        onOutgoingCallEnded(context, savedNumber, callStartTime, new Date());
                    }
                    break;
            }
            lastState = state;
        }

    }

    public class CallReceiver extends PhonecallReceiver {

        @Override
        protected void onIncomingCallReceived(Context ctx, String number, Date start) {
            Log.d("onIncomingCallReceived", number + " " + start.toString());
        }

        @Override
        protected void onIncomingCallAnswered(Context ctx, String number, Date start) {
            Log.d("onIncomingCallAnswered", number + " " + start.toString());
        }

        @Override
        protected void onIncomingCallEnded(Context ctx, String number, Date start, Date end) {
            Log.d("onIncomingCallEnded", number + " " + start.toString() + "\t" + end.toString());
        }

        @Override
        protected void onOutgoingCallStarted(Context ctx, String number, Date start) {
            Log.d("onOutgoingCallStarted", number + " " + start.toString());
        }

        @Override
        protected void onOutgoingCallEnded(Context ctx, String number, Date start, Date end) {
            Log.d("onOutgoingCallEnded", number + " " + start.toString() + "\t" + end.toString());
        }

        @Override
        protected void onMissedCall(Context ctx, String number, Date start) {
            Log.d("onMissedCall", number + " " + start.toString());
        }

    }

}