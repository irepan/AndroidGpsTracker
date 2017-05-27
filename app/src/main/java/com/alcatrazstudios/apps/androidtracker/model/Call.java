package com.alcatrazstudios.apps.androidtracker.model;

import java.util.Date;

/**
 * Created by irepan on 27/02/17.
 */

public class Call {
    private Date date;
    private String number;
    private String type;
    private String duration;
    public Call(){}
    public Call (Date date, String number, String type, String duration){
        this.date=date;
        this.number=number;
        this.type=type;
        this.duration=duration;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }
}
