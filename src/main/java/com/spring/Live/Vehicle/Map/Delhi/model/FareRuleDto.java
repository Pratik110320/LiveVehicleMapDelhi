package com.spring.Live.Vehicle.Map.Delhi.model;

import com.opencsv.bean.CsvBindByName;

import java.io.Serializable;

public class FareRuleDto implements Serializable {
    @CsvBindByName(column = "fare_id")
    private String fareId;
    @CsvBindByName(column = "route_id")
    private String routeId;
    @CsvBindByName(column = "origin_id")
    private String originId;
    @CsvBindByName(column = "destination_id")
    private String destinationId;

    private FareAttributeDto fareAttribute;
    private RouteDto route;
    private StopDto originStop;
    private StopDto destinationStop;

    public FareRuleDto() {}

    // Getters and Setters
    public String getFareId() { return fareId; }
    public void setFareId(String fareId) { this.fareId = fareId; }
    public String getRouteId() { return routeId; }
    public void setRouteId(String routeId) { this.routeId = routeId; }
    public String getOriginId() { return originId; }
    public void setOriginId(String originId) { this.originId = originId; }
    public String getDestinationId() { return destinationId; }
    public void setDestinationId(String destinationId) { this.destinationId = destinationId; }

    public FareAttributeDto getFareAttribute() { return fareAttribute; }
    public void setFareAttribute(FareAttributeDto fareAttribute) { this.fareAttribute = fareAttribute; }

    public RouteDto getRoute() { return route; }
    public void setRoute(RouteDto route) { this.route = route; }

    public StopDto getOriginStop() { return originStop; }
    public void setOriginStop(StopDto originStop) { this.originStop = originStop; }

    public StopDto getDestinationStop() { return destinationStop; }
    public void setDestinationStop(StopDto destinationStop) { this.destinationStop = destinationStop; }
}



