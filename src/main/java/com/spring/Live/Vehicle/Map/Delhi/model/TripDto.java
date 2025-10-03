package com.spring.Live.Vehicle.Map.Delhi.model;

import com.opencsv.bean.CsvBindByName;

public class TripDto {
    @CsvBindByName(column = "route_id")
    private String routeId;
    @CsvBindByName(column = "service_id")
    private String serviceId;
    @CsvBindByName(column = "trip_id")
    private String tripId;
    @CsvBindByName(column = "trip_headsign")
    private String tripHeadsign;
    @CsvBindByName(column = "direction_id")
    private int directionId;

    // Getters and Setters
    public String getRouteId() { return routeId; }
    public void setRouteId(String routeId) { this.routeId = routeId; }
    public String getServiceId() { return serviceId; }
    public void setServiceId(String serviceId) { this.serviceId = serviceId; }
    public String getTripId() { return tripId; }
    public void setTripId(String tripId) { this.tripId = tripId; }
    public String getTripHeadsign() { return tripHeadsign; }
    public void setTripHeadsign(String tripHeadsign) { this.tripHeadsign = tripHeadsign; }
    public int getDirectionId() { return directionId; }
    public void setDirectionId(int directionId) { this.directionId = directionId; }
}
