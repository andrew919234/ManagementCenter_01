package com.example.managementcenter.Resources;

public class Shift {
    private String shiftName;
    private double duration;
    private String startTime;
    private String endTime;
    

    public Shift(String shiftName, double duration, String startTime, String endTime) {
        this.shiftName = shiftName;
        this.duration = duration;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public String getShiftName() {
        return shiftName;
    }

    public void setShiftName(String shiftName) {
        this.shiftName = shiftName;
    }

    public double getDuration() {
    	double duration = Math.round(this.duration * 10.0) / 10.0;
        return duration;
    }

    public void setDuration(double duration) {
        double durationf = Math.round(duration * 10.0) / 10.0;
        this.duration = durationf;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }
}
