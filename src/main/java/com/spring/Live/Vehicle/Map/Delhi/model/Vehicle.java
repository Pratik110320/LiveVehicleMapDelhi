package com.spring.Live.Vehicle.Map.Delhi.model;

import jakarta.persistence.Entity;


public class Vehicle {
    private String vehicleId;
    private String tripId;
    private String routeId;
    private String routeName;
    private String tripHeadsign;
    private double lat;
    private double lon;
    private double speed;
    private Long timestamp;       // from feed
    private Long lastUpdated;     // internal store
    private String occupancyStatus;
    private String vehicleCategory;

    public Vehicle() {}

    public Vehicle(String vehicleId, double lat, double lon, String routeId, String tripId, Long timestamp) {
        this.vehicleId = vehicleId;
        this.lat = lat;
        this.lon = lon;
        this.routeId = routeId;
        this.tripId = tripId;
        this.timestamp = timestamp;
    }

    // Getters & Setters
    public String getVehicleId() { return vehicleId; }
    public void setVehicleId(String vehicleId) { this.vehicleId = vehicleId; }
    public String getTripId() { return tripId; }
    public void setTripId(String tripId) { this.tripId = tripId; }
    public String getRouteId() { return routeId; }
    public void setRouteId(String routeId) { this.routeId = routeId; }
    public String getRouteName() { return routeName; }
    public void setRouteName(String routeName) { this.routeName = routeName; }
    public String getTripHeadsign() { return tripHeadsign; }
    public void setTripHeadsign(String tripHeadsign) { this.tripHeadsign = tripHeadsign; }
    public double getLat() { return lat; }
    public void setLat(double lat) { this.lat = lat; }
    public double getLon() { return lon; }
    public void setLon(double lon) { this.lon = lon; }
    public double getSpeed() { return speed; }
    public void setSpeed(double speed) { this.speed = speed; }
    public Long getTimestamp() { return timestamp; }
    public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }
    public Long getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(Long lastUpdated) { this.lastUpdated = lastUpdated; }
    public String getOccupancyStatus() { return occupancyStatus; }
    public void setOccupancyStatus(String occupancyStatus) { this.occupancyStatus = occupancyStatus; }
    public String getVehicleCategory() { return vehicleCategory; }
    public void setVehicleCategory(String vehicleCategory) { this.vehicleCategory = vehicleCategory; }
}
