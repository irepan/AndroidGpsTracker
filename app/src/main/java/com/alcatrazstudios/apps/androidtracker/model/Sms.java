package com.alcatrazstudios.apps.androidtracker.model;

import java.util.Date;

/**
 * Created by irepan on 27/02/17.
 */
// //{ID, THREAD_ID, ADDRESS, PERSON, DATE, READ, STATUS, TYPE, BODY, SEEN};
public class Sms{
    private long id;
    private long thread_id;
    private String type;
    private String address;
    private long contactId;
    private Date date;
    private String readState; //"0" for have not read sms and "1" for have read sms
    private String status; //"0" for have not read sms and "1" for have read sms
    private String msg;
    private String seen; //"0" for have not read sms and "1" for have read sms
    public Sms(){}

    public Sms(long id, long thread_id, String address, long contactId, Date date, String readState,String status, String type, String msg, String seen){
        this.id=id;
        this.thread_id=thread_id;
        this.contactId=contactId;
        this.date=date;
        this.readState=readState;
        this.status=status;
        this.type=type;
        this.address=address;
        this.msg=msg;
        this.seen=seen;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getThread_id() {
        return thread_id;
    }

    public void setThread_id(long thread_id) {
        this.thread_id = thread_id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getReadState() {
        return readState;
    }

    public void setReadState(String readState) {
        this.readState = readState;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public long getContactId() {
        return contactId;
    }

    public void setContactId(long contactId) {
        this.contactId = contactId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSeen() {
        return seen;
    }

    public void setSeen(String seen) {
        this.seen = seen;
    }
}
