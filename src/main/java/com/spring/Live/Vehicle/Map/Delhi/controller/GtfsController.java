package com.spring.Live.Vehicle.Map.Delhi.controller;

import com.spring.Live.Vehicle.Map.Delhi.model.*;
import com.spring.Live.Vehicle.Map.Delhi.service.GtfsStaticService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/api")
public class GtfsController {

    private final GtfsStaticService gtfsStaticService;

    @Autowired
    public GtfsController(GtfsStaticService gtfsStaticService) {
        this.gtfsStaticService = gtfsStaticService;
    }

    @GetMapping("/routes")
    public Collection<RouteDto> getActiveRoutes() {
        return gtfsStaticService.getActiveRoutes();
    }

    @GetMapping("/stops/all")
    public Collection<StopDto> getAllStops() {
        return gtfsStaticService.getAllStops();
    }

    @GetMapping("/routes/{routeId}/stops")
    public List<StopDto> getStopsByRoute(@PathVariable String routeId) {
        return gtfsStaticService.getStopsByRouteId(routeId);
    }

    /**
     * Returns an ordered list of stops representing the path for a given route.
     * This is used to draw the polyline on the map.
     */
    @GetMapping("/routes/{routeId}/path")
    public List<StopDto> getRoutePath(@PathVariable String routeId) {
        return gtfsStaticService.getStopsByRouteId(routeId);
    }

    @GetMapping("/stops/{stopId}/routes")
    public List<RouteDto> getRoutesForStop(@PathVariable String stopId) {
        return gtfsStaticService.getRoutesForStop(stopId);
    }

    @GetMapping("/trips/{tripId}/schedule")
    public List<ScheduleItemDto> getTripSchedule(@PathVariable String tripId) {
        return gtfsStaticService.getScheduleForTrip(tripId);
    }

    @GetMapping("/stops/search")
    public List<StopDto> searchStops(@RequestParam("q") String query) {
        return gtfsStaticService.searchStopsByName(query);
    }

    @GetMapping("/fare")
    public ResponseEntity<FareAttributeDto> getFare(
            @RequestParam String routeId,
            @RequestParam String fromStopId,
            @RequestParam String toStopId) {
        FareAttributeDto fare = gtfsStaticService.getFare(routeId, fromStopId, toStopId);
        if (fare != null) {
            return ResponseEntity.ok(fare);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}

