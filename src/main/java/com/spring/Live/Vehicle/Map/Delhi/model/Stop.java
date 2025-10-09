package com.spring.Live.Vehicle.Map.Delhi.model;

import com.opencsv.bean.CsvBindByName;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "stops")
public class Stop {

    @Id
    @CsvBindByName(column = "stop_id")
    @Column(name = "stop_id")
    private String stopId;

    @CsvBindByName(column = "stop_name")
    @Column(name = "stop_name")
    private String stopName;

    @CsvBindByName(column = "stop_lat")
    @Column(name = "stop_lat")
    private double stopLat;

    @CsvBindByName(column = "stop_lon")
    @Column(name = "stop_lon")
    private double stopLon;

    @CsvBindByName(column = "stop_code")
    @Column(name = "stop_code")
    private String stopCode;

    @CsvBindByName(column = "zone_id")
    @Column(name = "zone_id")
    private String zoneId;

    public Stop() {
    }

    public String getStopId() {
        return stopId;
    }

    public void setStopId(String stopId) {
        this.stopId = stopId;
    }

    public String getStopName() {
        return stopName;
    }

    public void setStopName(String stopName) {
        this.stopName = stopName;
    }

    public double getStopLat() {
        return stopLat;
    }

    public void setStopLat(double stopLat) {
        this.stopLat = stopLat;
    }

    public double getStopLon() {
        return stopLon;
    }

    public void setStopLon(double stopLon) {
        this.stopLon = stopLon;
    }

    public String getStopCode() {
        return stopCode;
    }

    public void setStopCode(String stopCode) {
        this.stopCode = stopCode;
    }

    public String getZoneId() {
        return zoneId;
    }

    public void setZoneId(String zoneId) {
        this.zoneId = zoneId;
    }
}
