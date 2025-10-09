package com.spring.Live.Vehicle.Map.Delhi.controller;

import com.spring.Live.Vehicle.Map.Delhi.service.TransitService;
import com.spring.Live.Vehicle.Map.Delhi.service.TransitService.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transit")
@CrossOrigin(origins = "*")
public class TransitController {

    @Autowired
    private TransitService transitService;

    // ===============================
    // FIND NEARBY STOPS
    // ===============================
    /**
     * GET /api/transit/stops/nearby?lat=28.6139&lon=77.2090&limit=5
     * Find stops near user's location
     */
    @GetMapping("/stops/nearby")
    public ResponseEntity<List<NearbyStopDTO>> getNearbyStops(
            @RequestParam double lat,
            @RequestParam double lon,
            @RequestParam(defaultValue = "2") int limit) {

        List<NearbyStopDTO> nearbyStops = transitService.findNearestStops(lat, lon, limit);
        return ResponseEntity.ok(nearbyStops);
    }

    // ===============================
    // ROUTE PLANNING (A to B)
    // ===============================
    /**
     * GET /api/transit/routes/plan?origin=STOP001&dest=STOP150
     * Find routes between two stops
     */
    @GetMapping("/routes/plan")
    public ResponseEntity<List<RouteOption>> planRoute(
            @RequestParam String origin,
            @RequestParam String dest) {

        List<RouteOption> routes = transitService.findRoutes(origin, dest);

        if (routes.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(routes);
    }

    // ===============================
    // NEXT BUS ARRIVALS
    // ===============================
    /**
     * GET /api/transit/stops/STOP001/next-buses?limit=5
     * Get next arriving buses at a stop
     */
    @GetMapping("/stops/{stopId}/next-buses")
    public ResponseEntity<List<NextBusDTO>> getNextBuses(
            @PathVariable String stopId,
            @RequestParam(defaultValue = "5") int limit) {

        List<NextBusDTO> nextBuses = transitService.getNextBuses(stopId, limit);
        return ResponseEntity.ok(nextBuses);
    }

    // ===============================
    // FARE CALCULATION
    // ===============================
    /**
     * GET /api/transit/fare?origin=STOP001&dest=STOP150&route=ROUTE_X
     * Calculate fare for a journey
     */
    @GetMapping("/fare")
    public ResponseEntity<FareInfo> calculateFare(
            @RequestParam String origin,
            @RequestParam String dest,
            @RequestParam(required = false) String route) {

        FareInfo fareInfo = transitService.calculateFare(origin, dest, route);
        return ResponseEntity.ok(fareInfo);
    }

    // ===============================
    // ROUTE DETAILS
    // ===============================
    /**
     * GET /api/transit/routes/ROUTE_X/details
     * Get complete route information with all stops
     */
    @GetMapping("/routes/{routeId}/details")
    public ResponseEntity<RouteDetails> getRouteDetails(
            @PathVariable String routeId) {

        RouteDetails details = transitService.getRouteDetails(routeId);

        if (details == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(details);
    }

    // ===============================
    // SEARCH STOPS
    // ===============================
    /**
     * GET /api/transit/stops/search?q=central
     * Search stops by name or code
     */
    @GetMapping("/stops/search")
    public ResponseEntity<List<com.spring.Live.Vehicle.Map.Delhi.model.Stop>> searchStops(
            @RequestParam String q) {

        List<com.spring.Live.Vehicle.Map.Delhi.model.Stop> stops =
                transitService.searchStops(q);
        return ResponseEntity.ok(stops);
    }

    // ===============================
    // SEARCH ROUTES
    // ===============================
    /**
     * GET /api/transit/routes/search?q=express
     * Search routes by name
     */
    @GetMapping("/routes/search")
    public ResponseEntity<List<com.spring.Live.Vehicle.Map.Delhi.model.Route>> searchRoutes(
            @RequestParam String q) {

        List<com.spring.Live.Vehicle.Map.Delhi.model.Route> routes =
                transitService.searchRoutes(q);
        return ResponseEntity.ok(routes);
    }

    // ===============================
    // HEALTH CHECK
    // ===============================
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Transit API is running");
    }
}