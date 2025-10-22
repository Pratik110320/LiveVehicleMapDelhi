package com.spring.Live.Vehicle.Map.Delhi.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "stops")
public class Stop {

    @Id
    @Column(name = "stop_id")
    private String stopId;

    @Column(name = "stop_name")
    private String stopName;

    @Column(name = "stop_lat")
    private double stopLat;

    @Column(name = "stop_lon")
    private double stopLon;

    // getters and setters
    public String getStopId() { return stopId; }
    public void setStopId(String stopId) { this.stopId = stopId; }
    public String getStopName() { return stopName; }
    public void setStopName(String stopName) { this.stopName = stopName; }
    public double getStopLat() { return stopLat; }
    public void setStopLat(double stopLat) { this.stopLat = stopLat; }
    public double getStopLon() { return stopLon; }
    public void setStopLon(double stopLon) { this.stopLon = stopLon; }
}
