package com.spring.Live.Vehicle.Map.Delhi.model;


public class VehiclePosition {
    private String vehicleId;
    private String routeId;
    private double latitude;
    private double longitude;
    private Float bearing;
    private Float speed;
    private Long timestamp; // epoch seconds

    public VehiclePosition() { }

    public String getVehicleId() {
        return vehicleId;
    }
    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }
    public String getRouteId() {
        return routeId;
    }
    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }
    public double getLatitude() {
        return latitude;
    }
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
    public double getLongitude() {
        return longitude;
    }
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
    public Float getBearing() {
        return bearing;
    }
    public void setBearing(Float bearing) {
        this.bearing = bearing;
    }
    public Float getSpeed() {
        return speed;
    }
    public void setSpeed(Float speed) {
        this.speed = speed;
    }
    public Long getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}
