package com.alcatrazstudios.apps.androidtracker.model;

import java.io.File;
import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

/**
 * Created by irepan on 02/05/17.
 */

public class CallRecordings extends RealmObject {
/*    @Ignore
    public static final String IN_CALL="In";
    @Ignore
    public static final String OUT_CALL="Out";*/
    @PrimaryKey
    private String fileName;
    @Required
    private String filePath;
    @Required
    private String type;
    @Required
    private String number;
    @Required
    private Date start;
    @Required
    private Date end;
    private boolean uploaded = false;
    private boolean setToUpload = false;


    public CallRecordings(){
        super();
    }

    public CallRecordings(String type, File file, String number,Date start,Date end){
        this.type=type;
        this.filePath=file.getParent();
        this.fileName =file.getName();
        this.number=number;
        this.start=start;
        this.end=end;
        this.uploaded=false;
        this.setToUpload=false;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
    }

    public boolean isUploaded() {
        return uploaded;
    }

    public void setUploaded(boolean uploaded) {
        this.uploaded = uploaded;
    }

    public boolean isSetToUpload() {
        return setToUpload;
    }

    public void setSetToUpload(boolean setToUpload) {
        this.setToUpload = setToUpload;
    }
}
