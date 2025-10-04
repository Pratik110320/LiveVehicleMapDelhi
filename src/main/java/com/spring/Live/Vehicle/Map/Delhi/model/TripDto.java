package com.spring.Live.Vehicle.Map.Delhi.model;

import com.opencsv.bean.CsvBindByName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TripDto implements Serializable {
    @CsvBindByName(column = "route_id")
    private String routeId;
    @CsvBindByName(column = "service_id")
    private String serviceId;
    @CsvBindByName(column = "trip_id")
    private String tripId;

    @CsvBindByName(column = "shape_id")
    private String shapeId;

    @CsvBindByName(column = "trip_headsign")
    private String tripHeadsign;
    @CsvBindByName(column = "direction_id")
    private int directionId;


    private RouteDto route;
    private List<StopTimeDto> stopTimes = new ArrayList<>();
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
    public String getShape_id() { return shapeId; }
    public void setShape_id(String shape_id) { this.shapeId = shapeId; }

    public List<StopTimeDto> getStopTimes() { return stopTimes; }
    public void setStopTimes(List<StopTimeDto> stopTimes) { this.stopTimes = stopTimes; }
    public void addStopTime(StopTimeDto st) {
        if (this.stopTimes == null) this.stopTimes = new ArrayList<>();
        this.stopTimes.add(st);
    }
}
