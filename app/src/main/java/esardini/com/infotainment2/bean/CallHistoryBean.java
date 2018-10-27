package esardini.com.infotainment2.bean;

import java.util.Date;

public class CallHistoryBean {

    private String phNumber;
    private String callType;
    private String callTypeDesc;
    private String callDate;
    private Date callDayTime;
    private String callDuration;
    private String name;

    public CallHistoryBean(String phNumber, String callType, String callTypeDesc, String callDate, Date callDayTime, String callDuration, String name) {
        this.phNumber = phNumber;
        this.callType = callType;
        this.callTypeDesc = callTypeDesc;
        this.callDate = callDate;
        this.callDayTime = callDayTime;
        this.callDuration = callDuration;
        this.name = name;
    }

    public String getPhNumber() {
        return phNumber;
    }

    public void setPhNumber(String phNumber) {
        this.phNumber = phNumber;
    }

    public String getCallType() {
        return callType;
    }

    public void setCallType(String callType) {
        this.callType = callType;
    }

    public String getCallDate() {
        return callDate;
    }

    public void setCallDate(String callDate) {
        this.callDate = callDate;
    }

    public Date getCallDayTime() {
        return callDayTime;
    }

    public void setCallDayTime(Date callDayTime) {
        this.callDayTime = callDayTime;
    }

    public String getCallDuration() {
        return callDuration;
    }

    public void setCallDuration(String callDuration) {
        this.callDuration = callDuration;
    }
}
