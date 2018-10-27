package com.example.esardini.infotainment.bean;

public class CallBean extends ContactBean {

    private String date;
    private String type;

    public CallBean(String name, String mobileNumber, String date, String type) {
        this.setName(name);
        this.setNumber(mobileNumber);
        this.date = date;
        this.type = type;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
