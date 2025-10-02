package com.spring.Live.Vehicle.Map.Delhi.model;

public class FareRuleDto {
    private String fareId;
    private String routeId;
    private String origin_id;
    private String destination_id;
    // Getters and Setters

    public String getOrigin_id() {
        return origin_id;
    }

    public void setOrigin_id(String origin_id) {
        this.origin_id = origin_id;
    }

    public String getDestination_id() {
        return destination_id;
    }

    public void setDestination_id(String destination_id) {
        this.destination_id = destination_id;
    }

    public String getFareId() { return fareId; }
    public void setFareId(String fareId) { this.fareId = fareId; }
    public String getRouteId() { return routeId; }
    public void setRouteId(String routeId) { this.routeId = routeId; }
}
