package com.alcatrazstudios.apps.androidtracker.config;

import android.content.SharedPreferences;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by irepan on 28/03/17.
 */

public class TrackerConfiguration {

    public static final String CALL_LOG_INTERVAL="tiempocall";
    public static final String CALL_REC_INTERVAL="tiempograba";
    public static final String SMS_LOG_INTERVAL="tiemposms";
    public static final String GPS_READ_INTERVAL="tiempogps";
    public static final String READ_CONFIG_INTERVAL="tiempoconfig";
    public static final String FILE_HISTORY_DAYS="dias_de_vida";
    public static final String MIN_ACCURACY="minexactitud";
    public static final String MIN_DISTANCE="mindistancia";
    public static final String RECORD_CALLS="grabar";
    public static final String MEDIA_TYPE="tipomedios";


    private float gpsMinAccuracy;
    private float gpsMinDistance;
    private int gpsReaderFrequencyMinutes;
    private int smsReaderFrequencyMinutes;
    private int callLogFrequencyMinutes;
    private int commandReaderFrequencyMinutes;
    private int callRecordUpFrequencyMinutes;
    private int callHistoryLimitDays;
    private int recordingMediaType;
    private boolean recordCalls;

    public TrackerConfiguration() {
        super();
    }

    public TrackerConfiguration(SharedPreferences sharedPreferences) {
        super();
        this.gpsMinAccuracy=sharedPreferences.getFloat("gpsMinAccuracy",50F);
        this.gpsMinDistance=sharedPreferences.getFloat("gpdMinDistance",50F);
        this.gpsReaderFrequencyMinutes=sharedPreferences.getInt("gpsReaderFrequency",10);
        this.smsReaderFrequencyMinutes=sharedPreferences.getInt("smsReaderFrequency",60);
        this.callLogFrequencyMinutes=sharedPreferences.getInt("callLogReaderFrequency",60);
        this.commandReaderFrequencyMinutes=sharedPreferences.getInt("configurationFrequency",60);
        this.callRecordUpFrequencyMinutes=sharedPreferences.getInt("callRecUploadFrequency",60);
        this.callHistoryLimitDays=sharedPreferences.getInt("callHistoryLimitDays",14);
        this.recordingMediaType=sharedPreferences.getInt("recordingMediaType",1);
        this.recordCalls=sharedPreferences.getBoolean("recordCalls",true);
    }

    public TrackerConfiguration(JSONObject configuration) throws JSONException{
        super();
        this.gpsMinAccuracy=Float.valueOf(configuration.getLong((MIN_ACCURACY)));
        this.gpsMinDistance=Float.valueOf(configuration.getLong(MIN_DISTANCE));
        this.gpsReaderFrequencyMinutes=configuration.getInt(GPS_READ_INTERVAL);
        this.smsReaderFrequencyMinutes=configuration.getInt(SMS_LOG_INTERVAL);
        this.callLogFrequencyMinutes=configuration.getInt(CALL_LOG_INTERVAL);
        this.commandReaderFrequencyMinutes=configuration.getInt(READ_CONFIG_INTERVAL);
        this.callRecordUpFrequencyMinutes=configuration.getInt(CALL_REC_INTERVAL);
        this.callHistoryLimitDays=configuration.getInt(FILE_HISTORY_DAYS);
        this.recordingMediaType=configuration.getInt(MEDIA_TYPE);
        this.recordCalls=configuration.getInt(RECORD_CALLS)==1;
    }

    public void setPreferences(SharedPreferences sharedPreferences){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putFloat("gpsMinAccuracy",this.gpsMinAccuracy);
        editor.putFloat("gpdMinDistance",this.gpsMinDistance);
        editor.putInt("gpsReaderFrequency",this.gpsReaderFrequencyMinutes);
        editor.putInt("smsReaderFrequency",this.smsReaderFrequencyMinutes);
        editor.putInt("callLogReaderFrequency",this.callLogFrequencyMinutes);
        editor.putInt("configurationFrequency",this.commandReaderFrequencyMinutes);
        editor.putInt("callRecUploadFrequency",this.callRecordUpFrequencyMinutes);
        editor.putInt("callHistoryLimitDays",this.callHistoryLimitDays);
        editor.putInt("recordingMediaType",this.recordingMediaType);
        editor.putBoolean("recordCalls",this.recordCalls);
        editor.apply();
    }
    public float getGpsMinAccuracy() {
        return gpsMinAccuracy;
    }

    public void setGpsMinAccuracy(float gpsMinAccuracy) {
        this.gpsMinAccuracy = gpsMinAccuracy;
    }

    public float getGpsMinDistance() {
        return gpsMinDistance;
    }

    public void setGpsMinDistance(float gpsMinDistance) {
        this.gpsMinDistance = gpsMinDistance;
    }

    public int getGpsReaderFrequencyMinutes() {
        return gpsReaderFrequencyMinutes;
    }

    public void setGpsReaderFrequencyMinutes(int gpsReaderFrequencyMinutes) {
        this.gpsReaderFrequencyMinutes = gpsReaderFrequencyMinutes;
    }

    public int getSmsReaderFrequencyMinutes() {
        return smsReaderFrequencyMinutes;
    }

    public void setSmsReaderFrequencyMinutes(int smsReaderFrequencyMinutes) {
        this.smsReaderFrequencyMinutes = smsReaderFrequencyMinutes;
    }

    public int getCallLogFrequencyMinutes() {
        return callLogFrequencyMinutes;
    }

    public void setCallLogFrequencyMinutes(int callLogFrequencyMinutes) {
        this.callLogFrequencyMinutes = callLogFrequencyMinutes;
    }

    public int getCommandReaderFrequencyMinutes() {
        return commandReaderFrequencyMinutes;
    }

    public void setCommandReaderFrequencyMinutes(int commandReaderFrequencyMinutes) {
        this.commandReaderFrequencyMinutes = commandReaderFrequencyMinutes;
    }

    public boolean isRecordCalls() {
        return recordCalls;
    }

    public void setRecordCalls(boolean recordCalls) {
        this.recordCalls = recordCalls;
    }


    public boolean equals(TrackerConfiguration obj){
        return this.gpsMinAccuracy==obj.gpsMinAccuracy &&
                this.gpsMinDistance==obj.gpsMinDistance &&
                this.gpsReaderFrequencyMinutes==obj.gpsReaderFrequencyMinutes &&
                this.smsReaderFrequencyMinutes==obj.smsReaderFrequencyMinutes &&
                this.callLogFrequencyMinutes==obj.callLogFrequencyMinutes &&
                this.commandReaderFrequencyMinutes==obj.commandReaderFrequencyMinutes &&
                this.callRecordUpFrequencyMinutes==obj.callRecordUpFrequencyMinutes &&
                this.callHistoryLimitDays == obj.callHistoryLimitDays &&
                this.recordingMediaType == obj.recordingMediaType &&
                this.recordCalls==obj.recordCalls;
    }

    public boolean equals(SharedPreferences sharedPreferences){
        TrackerConfiguration obj=new TrackerConfiguration(sharedPreferences);
        return this.equals(obj);
    }

    public boolean isIntervalChanged(TrackerConfiguration obj){
        return !(this.gpsReaderFrequencyMinutes==obj.gpsReaderFrequencyMinutes &&
                this.smsReaderFrequencyMinutes==obj.smsReaderFrequencyMinutes &&
                this.callLogFrequencyMinutes==obj.callLogFrequencyMinutes &&
                this.commandReaderFrequencyMinutes==obj.commandReaderFrequencyMinutes &&
                this.callRecordUpFrequencyMinutes==obj.callRecordUpFrequencyMinutes);
    }

    public boolean isIntervalChanged(SharedPreferences sharedPreferences) {
        TrackerConfiguration obj = new TrackerConfiguration(sharedPreferences);
        return this.isIntervalChanged(obj);
    }

    public boolean isRecordCallsChanged(TrackerConfiguration obj){
        return this.isRecordCalls()==obj.isRecordCalls();
    }

    public boolean isRecordCallsChanged(SharedPreferences sharedPreferences){
        TrackerConfiguration obj = new TrackerConfiguration(sharedPreferences);
        return isRecordCallsChanged(obj);
    }

    public int getCallRecordUpFrequencyMinutes() {
        return callRecordUpFrequencyMinutes;
    }

    public void setCallRecordUpFrequencyMinutes(int callRecordUpFrequencyMinutes) {
        this.callRecordUpFrequencyMinutes = callRecordUpFrequencyMinutes;
    }

    public int getCallHistoryLimitDays() {
        return callHistoryLimitDays;
    }

    public void setCallHistoryLimitDays(int callHistoryLimitDays) {
        this.callHistoryLimitDays = callHistoryLimitDays;
    }

    public int getRecordingMediaType() {
        return recordingMediaType;
    }

    public void setRecordingMediaType(int recordingMediaType) {
        this.recordingMediaType = recordingMediaType;
    }
}
