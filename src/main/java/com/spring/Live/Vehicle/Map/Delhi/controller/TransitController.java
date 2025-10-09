package com.spring.Live.Vehicle.Map.Delhi.controller;

import com.spring.Live.Vehicle.Map.Delhi.model.Agency;
import com.spring.Live.Vehicle.Map.Delhi.model.Route;
import com.spring.Live.Vehicle.Map.Delhi.model.Stop;
import com.spring.Live.Vehicle.Map.Delhi.model.Trip;
import com.spring.Live.Vehicle.Map.Delhi.service.TransitService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/transit")
class TransitController {

    private final TransitService transitService;

    public TransitController(TransitService transitService) {
        this.transitService = transitService;
    }

    @GetMapping("/agencies")
    public List<Agency> getAgencies() {
        return transitService.getAllAgencies();
    }

    @GetMapping("/routes")
    public List<Route> getRoutes() {
        return transitService.getAllRoutes();
    }

    @GetMapping("/trips")
    public ResponseEntity<List<Trip>> getTrips(@RequestParam Optional<String> routeId) {
        List<Trip> trips = routeId.map(transitService::getTripsByRouteId)
                .orElseGet(transitService::getAllTrips);
        return ResponseEntity.ok(trips);
    }

    @GetMapping("/trips/{tripId}/stops")
    public ResponseEntity<List<Stop>> getStopsForTrip(@PathVariable String tripId) {
        List<Stop> stops = transitService.getStopsForTrip(tripId);
        if (stops.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(stops);
    }
}
