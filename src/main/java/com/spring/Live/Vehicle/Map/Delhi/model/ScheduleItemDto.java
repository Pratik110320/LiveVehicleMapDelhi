package com.spring.Live.Vehicle.Map.Delhi.model;

/**
 * A DTO that combines stop time information with detailed stop information
 * to represent a single entry in a trip's schedule.
 */
public class ScheduleItemDto {

    private String stopId;
    private String stopName;
    private double stopLat;
    private double stopLon;
    private String arrivalTime;
    private String departureTime;
    private int stopSequence;

    public ScheduleItemDto(StopDto stop, StopTimeDto stopTime) {
        if (stop == null || stopTime == null) {
            // Prevent NullPointerException if data is inconsistent
            return;
        }
        this.stopId = stop.getStopId();
        this.stopName = stop.getStopName();
        this.stopLat = stop.getStopLat();
        this.stopLon = stop.getStopLon();
        this.arrivalTime = stopTime.getArrivalTime();
        this.departureTime = stopTime.getDepartureTime();
        this.stopSequence = stopTime.getStopSequence();
    }

    // Getters and Setters
    public String getStopId() { return stopId; }
    public void setStopId(String stopId) { this.stopId = stopId; }
    public String getStopName() { return stopName; }
    public void setStopName(String stopName) { this.stopName = stopName; }
    public double getStopLat() { return stopLat; }
    public void setStopLat(double stopLat) { this.stopLat = stopLat; }
    public double getStopLon() { return stopLon; }
    public void setStopLon(double stopLon) { this.stopLon = stopLon; }
    public String getArrivalTime() { return arrivalTime; }
    public void setArrivalTime(String arrivalTime) { this.arrivalTime = arrivalTime; }
    public String getDepartureTime() { return departureTime; }
    public void setDepartureTime(String departureTime) { this.departureTime = departureTime; }
    public int getStopSequence() { return stopSequence; }
    public void setStopSequence(int stopSequence) { this.stopSequence = stopSequence; }
}
