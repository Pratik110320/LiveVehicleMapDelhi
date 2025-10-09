package com.spring.Live.Vehicle.Map.Delhi.model;

import com.opencsv.bean.CsvBindByName;
import jakarta.persistence.*;


@Entity
@Table(name = "fare_rules", indexes = {
        @Index(name = "idx_farerule_fare_id", columnList = "fare_id"),
        @Index(name = "idx_farerule_route_id", columnList = "route_id")
})
public class FareRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CsvBindByName(column = "fare_id")
    @Column(name = "fare_id")
    private String fareId;

    @CsvBindByName(column = "route_id")
    @Column(name = "route_id")
    private String routeId;

    @CsvBindByName(column = "origin_id")
    @Column(name = "origin_id")
    private String originId;

    @CsvBindByName(column = "destination_id")
    @Column(name = "destination_id")
    private String destinationId;

    public FareRule() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFareId() { return fareId; }
    public void setFareId(String fareId) { this.fareId = fareId; }
    public String getRouteId() { return routeId; }
    public void setRouteId(String routeId) { this.routeId = routeId; }
    public String getOriginId() { return originId; }
    public void setOriginId(String originId) { this.originId = originId; }
    public String getDestinationId() { return destinationId; }
    public void setDestinationId(String destinationId) { this.destinationId = destinationId; }
}


