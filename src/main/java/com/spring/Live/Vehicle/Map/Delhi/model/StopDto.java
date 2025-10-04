package com.spring.Live.Vehicle.Map.Delhi.model;

import com.opencsv.bean.CsvBindByName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class StopDto  implements Serializable {

    @CsvBindByName(column = "stop_code")
    private String stopCode;

    @CsvBindByName(column = "stop_id")
    private String stopId;
    @CsvBindByName(column = "stop_name")
    private String stopName;
    @CsvBindByName(column = "stop_lat")
    private double stopLat;
    @CsvBindByName(column = "stop_lon")
    private double stopLon;

    @CsvBindByName(column = "zone_id")
    private String zoneId;

    private List<StopTimeDto> stopTimes = new ArrayList<>();

    public StopDto() {}

    // Getters and Setters
    public String getStopCode() { return stopCode; }
    public void setStopCode(String stop_code) { this.stopCode = stopCode; }

    public String getStopId() { return stopId; }
    public void setStopId(String stopId) { this.stopId = stopId; }
    public String getStopName() { return stopName; }
    public void setStopName(String stopName) { this.stopName = stopName; }
    public double getStopLat() { return stopLat; }
    public void setStopLat(double stopLat) { this.stopLat = stopLat; }
    public double getStopLon() { return stopLon; }
    public void setStopLon(double stopLon) { this.stopLon = stopLon; }
    public String getZone_id() { return zoneId; }
    public void setZone_id(String zone_id) { this.zoneId = zoneId; }

    public List<StopTimeDto> getStopTimes() { return stopTimes; }
    public void setStopTimes(List<StopTimeDto> stopTimes) { this.stopTimes = stopTimes; }
    public void addStopTime(StopTimeDto st) {
        if (this.stopTimes == null) this.stopTimes = new ArrayList<>();
        this.stopTimes.add(st);
    }
}
