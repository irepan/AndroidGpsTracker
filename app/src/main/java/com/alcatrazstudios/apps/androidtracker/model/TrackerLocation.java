package com.alcatrazstudios.apps.androidtracker.model;

import android.location.Location;

import java.util.Date;

/**
 * Created by irepan on 28/02/17.
 */

public class TrackerLocation {
    private Double lat;
    private Double lon;
    private Double alt;
    private float speed;
    private float bearing;
    private float distance;
    private float accuracy;
    private Date date;



    public TrackerLocation(){}

    public TrackerLocation(Double lat, Double lon, Double alt, float speed, float bearing, float distance, float accuracy, Date date){
        this.lat=lat;
        this.lon=lon;
        this.alt=alt;
        this.speed=speed;
        this.bearing=bearing;
        this.distance=distance;
        this.accuracy=accuracy;
        this.date=date;
    }

    public TrackerLocation(Location location){
        this.lat=location.getLatitude();
        this.lon=location.getLongitude();
        this.alt=location.getAltitude();
        this.speed=location.getSpeed();
        this.bearing=location.getBearing();
        this.accuracy=location.getAccuracy();
        this.date=new Date(location.getTime());
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLon() {
        return lon;
    }

    public void setLon(Double lon) {
        this.lon = lon;
    }

    public Double getAlt() {
        return alt;
    }

    public void setAlt(Double alt) {
        this.alt = alt;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public float getBearing() {
        return bearing;
    }

    public void setBearing(float bearing) {
        this.bearing = bearing;
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public float getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(float accuracy) {
        this.accuracy = accuracy;
    }

}
