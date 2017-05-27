package com.alcatrazstudios.apps.androidtracker.model;

import com.alcatrazstudios.apps.androidtracker.application.GpsTrackerApplication;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

/**
 * Created by irepan on 24/02/17.
 */

public class GpsTrackerEvent extends RealmObject {
    @PrimaryKey
    private int id;
    private int type;
    @Required
    private String payLoad;
    @Required
    private Date date;
    @Required
    private Boolean uploaded;

    public GpsTrackerEvent() {}

    public GpsTrackerEvent(int type, String payLoad, Date date){
        this.id= GpsTrackerApplication.getInstance().getId(GpsTrackerEvent.class);
        this.type=type;
        this.payLoad=payLoad;
        this.date=date;
        this.uploaded=false;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getPayLoad() {
        return payLoad;
    }

    public void setPayLoad(String payLoad) {
        this.payLoad = payLoad;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Boolean getUploaded() {
        return uploaded;
    }

    public void setUploaded(Boolean uploaded) {
        this.uploaded = uploaded;
    }
}
