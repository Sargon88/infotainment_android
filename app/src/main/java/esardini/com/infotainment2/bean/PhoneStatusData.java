package esardini.com.infotainment2.bean;

import java.util.ArrayList;
import java.util.List;

public class PhoneStatusData {

    private String batt;
    private Boolean wifi;
    private Boolean bluetooth;
    private String signal;
    private ArrayList<ContactBean> starredcontacts;
    private List<CallHistoryBean> lastcalls;
    private String latitude;
    private String longitude;

    public String getBatt() {
        return batt;
    }

    public void setBatt(String batt) {
        this.batt = batt;
    }

    public Boolean getWifi() {
        return wifi;
    }

    public void setWifi(Boolean wifi) {
        this.wifi = wifi;
    }

    public Boolean getBluetooth() {
        return bluetooth;
    }

    public void setBluetooth(Boolean bluetooth) {
        this.bluetooth = bluetooth;
    }

    public String getSignal() {
        return signal;
    }

    public void setSignal(String signal) {
        this.signal = signal;
    }

    public ArrayList<ContactBean> getStarredcontacts() {
        return starredcontacts;
    }

    public void setStarredcontacts(ArrayList<ContactBean> starredcontacts) {
        this.starredcontacts = starredcontacts;
    }

    public List<CallHistoryBean> getLastcalls() {
        return lastcalls;
    }

    public void setLastcalls(List<CallHistoryBean> lastcalls) {
        this.lastcalls = lastcalls;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }
}
