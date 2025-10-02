package com.spring.Live.Vehicle.Map.Delhi.model;

public class RouteDto {
    private String routeId;
    private String agencyId;
    private String routeShortName;
    private String routeLongName;
    private int routeType;

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
}
