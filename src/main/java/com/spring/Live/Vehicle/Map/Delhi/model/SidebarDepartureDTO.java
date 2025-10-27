package com.spring.Live.Vehicle.Map.Delhi.model;

public class SidebarDepartureDTO {

    private String stopId;
    private String stopName;
    private String routeId;
    private String routeShortName;
    private String routeLongName;
    private String arrivalTime;


    private transient long diffSec;

    // Constructor for JPA Projections
    public SidebarDepartureDTO(String stopId, String stopName, String routeId, String routeShortName, String routeLongName, String arrivalTime) {
        this.stopId = stopId;
        this.stopName = stopName;
        this.routeId = routeId;
        this.routeShortName = routeShortName;
        this.routeLongName = routeLongName;
        this.arrivalTime = arrivalTime;
    }

    // Getters and Setters
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

    public String getRouteId() {
        return routeId;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    public String getRouteShortName() {
        return routeShortName;
    }

    public void setRouteShortName(String routeShortName) {
        this.routeShortName = routeShortName;
    }

    public String getRouteLongName() {
        return routeLongName;
    }

    public void setRouteLongName(String routeLongName) {
        this.routeLongName = routeLongName;
    }

    public String getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(String arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public long getDiffSec() {
        return diffSec;
    }

    public void setDiffSec(long diffSec) {
        this.diffSec = diffSec;
    }
}
