package com.spring.Live.Vehicle.Map.Delhi.model;

import com.opencsv.bean.CsvBindByName;
import jakarta.persistence.*;

@Entity
@Table(name = "routes", indexes = {
        @Index(name = "idx_route_agency_id", columnList = "agency_id")
})
public class Route {

    @Id
    @CsvBindByName(column = "route_id")
    @Column(name = "route_id")
    private String routeId;

    @CsvBindByName(column = "agency_id")
    @Column(name = "agency_id")
    private String agencyId;

    @CsvBindByName(column = "route_short_name")
    @Column(name = "route_short_name")
    private String routeShortName;

    @CsvBindByName(column = "route_long_name")
    @Column(name = "route_long_name")
    private String routeLongName;

    @CsvBindByName(column = "route_type")
    @Column(name = "route_type")
    private int routeType;

    public Route() {
    }

    public Route(String routeId, String agencyId, String routeShortName, String routeLongName, int routeType) {
        this.routeId = routeId;
        this.agencyId = agencyId;
        this.routeShortName = routeShortName;
        this.routeLongName = routeLongName;
        this.routeType = routeType;
    }

    public String getRouteId() {
        return routeId;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    public String getAgencyId() {
        return agencyId;
    }

    public void setAgencyId(String agencyId) {
        this.agencyId = agencyId;
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

    public int getRouteType() {
        return routeType;
    }

    public void setRouteType(int routeType) {
        this.routeType = routeType;
    }
}
