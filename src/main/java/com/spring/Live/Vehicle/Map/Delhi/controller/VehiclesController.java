package com.spring.Live.Vehicle.Map.Delhi.controller;


import com.spring.Live.Vehicle.Map.Delhi.model.*;
import com.spring.Live.Vehicle.Map.Delhi.service.GtfsStaticService;
import com.spring.Live.Vehicle.Map.Delhi.service.VehicleStoreService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import java.time.Instant;
import java.util.List;


@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") // Allow all origins for simplicity
public class VehiclesController {
    private final VehicleStoreService store;
    private final GtfsStaticService staticService;

    public VehiclesController(VehicleStoreService store, GtfsStaticService staticService) {
        this.store = store;
        this.staticService = staticService;
    }

    @GetMapping("/vehicles")
    public ResponseEntity<List<VehicleDto>> vehicles() {
        return ResponseEntity.ok(store.getAll());
    }

    @GetMapping("/stops")
    public ResponseEntity<List<StopDto>> stops() {
        return ResponseEntity.ok(staticService.getAllStops());
    }

    @GetMapping("/agencies")
    public ResponseEntity<List<AgencyDto>> agencies() {
        return ResponseEntity.ok(staticService.getAllAgencies());
    }

    @GetMapping("/calendars")
    public ResponseEntity<List<CalendarDto>> calendars() {
        return ResponseEntity.ok(staticService.getAllCalendars());
    }

    // ** REMOVED: Endpoint that returned all fare attributes, causing memory error **

    @GetMapping("/fare-rules/{routeId}")
    public ResponseEntity<List<FareRuleDto>> fareRules(@PathVariable String routeId) {
        return ResponseEntity.ok(staticService.getFareRulesForRoute(routeId));
    }

    @GetMapping("/stop-times/{tripId}")
    public ResponseEntity<List<StopTimeDto>> stopTimes(@PathVariable String tripId) {
        List<StopTimeDto> stopTimes = staticService.getStopTimesForTrip(tripId);
        if (stopTimes.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(stopTimes);
    }

    @GetMapping("/route-stops/{tripId}")
    public ResponseEntity<List<StopDto>> getRouteStops(@PathVariable String tripId) {
        List<StopDto> stops = staticService.getRouteStopsForTrip(tripId);
        if (stops.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(stops);
    }

    // ** NEW: Endpoint to get specific fares for a route **
    @GetMapping("/fares/{routeId}")
    public ResponseEntity<List<FareAttributeDto>> getFaresForRoute(@PathVariable String routeId) {
        List<FareAttributeDto> fares = staticService.getFareAttributesForRoute(routeId);
        if (fares.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(fares);
    }


    @GetMapping("/status")
    public ResponseEntity<?> status() {
        Long ts = store.getLastFeedTs();
        long age = ts == null ? -1 : Instant.now().getEpochSecond() - ts;
        return ResponseEntity.ok(
                java.util.Map.of(
                        "feedUrlSet", true,
                        "lastFeedTimestamp", ts,
                        "feedAgeSeconds", age
                )
        );
    }

    @GetMapping("/vehicles/{id}")
    public ResponseEntity<VehicleDto> getVehicle(@PathVariable String id) {
        return store.getAll().stream()
                .filter(v -> id.equals(v.getVehicleId()) || id.equals(v.getTripId()))
                .findFirst()
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}