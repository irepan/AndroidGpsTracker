package com.alcatrazstudios.apps.androidtracker.config;

import android.content.SharedPreferences;

/**
 * Created by irepan on 28/03/17.
 */

public class TrackerPreferences {

    private boolean currentlyTracking;
    private boolean firstTimeLoadingApp;


    public TrackerPreferences(){

    }
    public TrackerPreferences(SharedPreferences sharedPreferences){
        this.currentlyTracking = sharedPreferences.getBoolean("currentlyTracking", true);
        this.firstTimeLoadingApp = sharedPreferences.getBoolean("firstTimeLoadingApp", true);
    }

    public boolean isCurrentlyTracking() {
        return currentlyTracking;
    }

    public void setCurrentlyTracking(boolean currentlyTracking) {
        this.currentlyTracking = currentlyTracking;
    }

    public boolean isFirstTimeLoadingApp() {
        return firstTimeLoadingApp;
    }

    public void setFirstTimeLoadingApp(boolean firstTimeLoadingApp) {
        this.firstTimeLoadingApp = firstTimeLoadingApp;
    }
}
