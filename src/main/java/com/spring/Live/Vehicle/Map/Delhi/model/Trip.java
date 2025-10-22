package com.spring.Live.Vehicle.Map.Delhi.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "trips")
public class Trip {

    @Id
    @Column(name = "trip_id")
    private String tripId;

    @Column(name = "route_id")
    private String routeId;

    @Column(name = "service_id")
    private String serviceId;

    @Column(name = "shape_id")
    private String shapeId;

    // getters and setters
    public String getTripId() { return tripId; }
    public void setTripId(String tripId) { this.tripId = tripId; }
    public String getRouteId() { return routeId; }
    public void setRouteId(String routeId) { this.routeId = routeId; }
    public String getServiceId() { return serviceId; }
    public void setServiceId(String serviceId) { this.serviceId = serviceId; }
    public String getShapeId() { return shapeId; }
    public void setShapeId(String shapeId) { this.shapeId = shapeId; }
}
