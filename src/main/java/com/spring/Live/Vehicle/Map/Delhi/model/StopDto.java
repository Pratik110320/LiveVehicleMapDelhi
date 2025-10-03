package com.spring.Live.Vehicle.Map.Delhi.model;

import com.opencsv.bean.CsvBindByName;

public class StopDto {
    @CsvBindByName(column = "stop_id")
    private String stopId;
    @CsvBindByName(column = "stop_name")
    private String stopName;
    @CsvBindByName(column = "stop_lat")
    private double stopLat;
    @CsvBindByName(column = "stop_lon")
    private double stopLon;

    public StopDto() {}

    // Getters and Setters
    public String getStopId() { return stopId; }
    public void setStopId(String stopId) { this.stopId = stopId; }
    public String getStopName() { return stopName; }
    public void setStopName(String stopName) { this.stopName = stopName; }
    public double getStopLat() { return stopLat; }
    public void setStopLat(double stopLat) { this.stopLat = stopLat; }
    public double getStopLon() { return stopLon; }
    public void setStopLon(double stopLon) { this.stopLon = stopLon; }
}
