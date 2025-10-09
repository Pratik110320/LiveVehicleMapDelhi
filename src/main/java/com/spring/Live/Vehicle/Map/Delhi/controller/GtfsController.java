package com.spring.Live.Vehicle.Map.Delhi.controller;

import com.spring.Live.Vehicle.Map.Delhi.model.Route;
import com.spring.Live.Vehicle.Map.Delhi.model.Stop;
import com.spring.Live.Vehicle.Map.Delhi.service.GtfsStaticService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * A read-only controller for accessing the static GTFS dataset.
 * This provides endpoints to query foundational data like routes and stops,
 * separating read operations from the main CRUD controllers.
 */
@RestController
@RequestMapping("/api/gtfs")
public class GtfsController {

    private final GtfsStaticService gtfsStaticService;

    @Autowired
    public GtfsController(GtfsStaticService gtfsStaticService) {
        this.gtfsStaticService = gtfsStaticService;
    }

    @GetMapping("/routes")
    public ResponseEntity<List<Route>> getAllRoutes() {
        return ResponseEntity.ok(gtfsStaticService.getAllRoutes());
    }

    @GetMapping("/routes/{routeId}")
    public ResponseEntity<Route> getRouteById(@PathVariable String routeId) {
        return gtfsStaticService.getRouteById(routeId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/stops")
    public ResponseEntity<List<Stop>> getAllStops() {
        return ResponseEntity.ok(gtfsStaticService.getAllStops());
    }

    @GetMapping("/stops/{stopId}")
    public ResponseEntity<Stop> getStopById(@PathVariable String stopId) {
        return gtfsStaticService.getStopById(stopId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
