package com.spring.Live.Vehicle.Map.Delhi.model;

import com.opencsv.bean.CsvBindByName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class RouteDto  implements Serializable {
    @CsvBindByName(column = "route_id")
    private String routeId;
    @CsvBindByName(column = "agency_id")
    private String agencyId;
    @CsvBindByName(column = "route_short_name")
    private String routeShortName;
    @CsvBindByName(column = "route_long_name")
    private String routeLongName;
    @CsvBindByName(column = "route_type")
    private int routeType;

    private List<TripDto> trips = new ArrayList<>();
    private List<FareRuleDto> fareRules = new ArrayList<>();

    public RouteDto() {}

    // Getters and Setters
    public String getRouteId() { return routeId; }
    public void setRouteId(String routeId) { this.routeId = routeId; }
    public String getAgencyId() { return agencyId; }
    public void setAgencyId(String agencyId) { this.agencyId = agencyId; }
    public String getRouteShortName() { return routeShortName; }
    public void setRouteShortName(String routeShortName) { this.routeShortName = routeShortName; }
    public String getRouteLongName() { return routeLongName; }
    public void setRouteLongName(String routeLongName) { this.routeLongName = routeLongName; }
    public int getRouteType() { return routeType; }
    public void setRouteType(int routeType) { this.routeType = routeType; }

    public List<TripDto> getTrips() { return trips; }
    public void setTrips(List<TripDto> trips) { this.trips = trips; }
    public void addTrip(TripDto trip) {
        if (this.trips == null) this.trips = new ArrayList<>();
        this.trips.add(trip);
    }

    public List<FareRuleDto> getFareRules() { return fareRules; }
    public void setFareRules(List<FareRuleDto> fareRules) { this.fareRules = fareRules; }
    public void addFareRule(FareRuleDto r) {
        if (this.fareRules == null) this.fareRules = new ArrayList<>();
        this.fareRules.add(r);
    }
}
