package com.spring.Live.Vehicle.Map.Delhi.controller;

import com.spring.Live.Vehicle.Map.Delhi.model.Trip;
import com.spring.Live.Vehicle.Map.Delhi.repository.ScheduleProjection;
import com.spring.Live.Vehicle.Map.Delhi.service.TransitService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transit")
public class TransitController {

    private final TransitService transitService;

    public TransitController(TransitService transitService) {
        this.transitService = transitService;
    }

    @GetMapping("/trips")
    public List<Trip> findTripsByRouteAndDirection(
            @RequestParam String routeId,
            @RequestParam int directionId) {
        return transitService.findTripsByRouteAndDirection(routeId, directionId);
    }

    @GetMapping("/stops/{stopId}/schedule")
    public List<ScheduleProjection> getScheduleForStop(@PathVariable String stopId) {
        return transitService.getScheduleForStop(stopId);
    }
}
