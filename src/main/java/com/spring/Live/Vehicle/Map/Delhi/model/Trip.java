package com.spring.Live.Vehicle.Map.Delhi.model;

import com.opencsv.bean.CsvBindByName;
import jakarta.persistence.*;


@Entity
@Table(name = "trips", indexes = {
        @Index(name = "idx_trip_route_id", columnList = "route_id")
})
public class Trip {

    @Id
    @CsvBindByName(column = "trip_id")
    @Column(name = "trip_id")
    private String tripId;

    @CsvBindByName(column = "route_id")
    @Column(name = "route_id")
    private String routeId;

    @CsvBindByName(column = "service_id")
    @Column(name = "service_id")
    private String serviceId;

    @CsvBindByName(column = "shape_id")
    @Column(name = "shape_id")
    private String shapeId;

    public Trip() {
    }

    public String getTripId() {
        return tripId;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }

    public String getRouteId() {
        return routeId;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getShapeId() {
        return shapeId;
    }

    public void setShapeId(String shapeId) {
        this.shapeId = shapeId;
    }
}
