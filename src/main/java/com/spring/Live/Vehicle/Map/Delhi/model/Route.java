package com.spring.Live.Vehicle.Map.Delhi.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "routes")
public class Route {

    @Id
    @Column(name = "route_id")
    private String routeId;

    @Column(name = "route_short_name")
    private String routeShortName;

    @Column(name = "route_long_name")
    private String routeLongName;

    @Column(name = "route_type")
    private String routeType;

    // getters and setters
    public String getRouteId() { return routeId; }
    public void setRouteId(String routeId) { this.routeId = routeId; }
    public String getRouteShortName() { return routeShortName; }
    public void setRouteShortName(String routeShortName) { this.routeShortName = routeShortName; }
    public String getRouteLongName() { return routeLongName; }
    public void setRouteLongName(String routeLongName) { this.routeLongName = routeLongName; }
    public String getRouteType() { return routeType; }
    public void setRouteType(String routeType) { this.routeType = routeType; }
}
