package com.spring.Live.Vehicle.Map.Delhi.model;

import com.opencsv.bean.CsvBindByName;

public class StopTimeDto {
    @CsvBindByName(column = "trip_id")
    private String tripId;
    @CsvBindByName(column = "arrival_time")
    private String arrivalTime;
    @CsvBindByName(column = "departure_time")
    private String departureTime;
    @CsvBindByName(column = "stop_id")
    private String stopId;
    @CsvBindByName(column = "stop_sequence")
    private int stopSequence;

    // Getters and Setters
    public String getTripId() { return tripId; }
    public void setTripId(String tripId) { this.tripId = tripId; }
    public String getArrivalTime() { return arrivalTime; }
    public void setArrivalTime(String arrivalTime) { this.arrivalTime = arrivalTime; }
    public String getDepartureTime() { return departureTime; }
    public void setDepartureTime(String departureTime) { this.departureTime = departureTime; }
    public String getStopId() { return stopId; }
    public void setStopId(String stopId) { this.stopId = stopId; }
    public int getStopSequence() { return stopSequence; }
    public void setStopSequence(int stopSequence) { this.stopSequence = stopSequence; }
}
