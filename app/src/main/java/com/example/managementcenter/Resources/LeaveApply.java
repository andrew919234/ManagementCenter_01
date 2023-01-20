package com.example.managementcenter.Resources;

import java.util.ArrayList;
import java.util.List;

public class LeaveApply {

    private String leave;
    private String userEmail;
    private List<Integer> dayOfMouth =new ArrayList<>();
    private String startDate;
    private String startTime;
    private String endDate;
    private String endTime;
    private float hours;
    private String reason;

    public LeaveApply() {
    }

    public LeaveApply(String leave, String userEmail, List<Integer> dayOfMouth, float hours) {
        this.leave = leave;
        this.userEmail = userEmail;
        this.dayOfMouth = dayOfMouth;
        this.hours = hours;
    }

    public LeaveApply(String leave, String userEmail, String startDate, String startTime, String endDate, String endTime, float hours, String reason, float salary) {
        this.leave = leave;
        this.userEmail = userEmail;
        this.startDate = startDate;
        this.startTime = startTime;
        this.endDate = endDate;
        this.endTime = endTime;
        this.hours = hours;
        this.reason = reason;
    }

    public List<Integer> getDayOfMouth() {
        return dayOfMouth;
    }

    public void setDayOfMouth(List<Integer> dayOfMouth) {
        this.dayOfMouth = dayOfMouth;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {

        this.userEmail = userEmail;
    }

    public String getLeave() {
        return leave;
    }

    public void setLeave(String leave) {
        this.leave = leave;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public float getHours() {
        return hours;
    }

    public void setHours(float hours) {
        this.hours = hours;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }





}
