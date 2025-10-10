package com.spring.Live.Vehicle.Map.Delhi.controller;

import com.spring.Live.Vehicle.Map.Delhi.model.Stop;
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
    // FIND NEARBY STOPS (PIN-BASED)
    // ===============================
    /**
     * GET /api/transit/stops/nearby?lat=28.6139&lon=77.2090&radius=1.0
     * Find stops near a pin location on map
     */
    @GetMapping("/stops/nearby")
    public ResponseEntity<List<NearbyStopDTO>> getNearbyStops(
            @RequestParam double lat,
            @RequestParam double lon,
            @RequestParam(defaultValue = "1.0") double radius) { // radius in km

        List<NearbyStopDTO> nearbyStops = transitService.findNearestStops(lat, lon, radius);
        return ResponseEntity.ok(nearbyStops);
    }

    // ===============================
    // ROUTE PLANNING (PIN-BASED)
    // ===============================
    /**
     * GET /api/transit/routes/plan?originLat=28.6139&originLon=77.2090&destLat=28.6542&destLon=77.2373
     * Find routes between two pin locations
     */
    @GetMapping("/routes/plan")
    public ResponseEntity<List<RouteOption>> planRoute(
            @RequestParam double originLat,
            @RequestParam double originLon,
            @RequestParam double destLat,
            @RequestParam double destLon) {

        List<RouteOption> routes = transitService.findRoutesByLocation(originLat, originLon, destLat, destLon);

        if (routes.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(routes);
    }

    // ===============================
    // NEXT BUS ARRIVALS (PIN-BASED)
    // ===============================
    /**
     * GET /api/transit/stops/nearby/next-buses?lat=28.6139&lon=77.2090&radius=0.5
     * Get next arriving buses near a pin location
     */
    @GetMapping("/stops/nearby/next-buses")
    public ResponseEntity<List<NextBusDTO>> getNextBusesNearby(
            @RequestParam double lat,
            @RequestParam double lon,
            @RequestParam(defaultValue = "0.5") double radius,
            @RequestParam(defaultValue = "5") int limit) {

        List<NextBusDTO> nextBuses = transitService.getNextBusesNearLocation(lat, lon, radius, limit);
        return ResponseEntity.ok(nextBuses);
    }

    // ===============================
    // FARE CALCULATION (PIN-BASED)
    // ===============================
    /**
     * GET /api/transit/fare/calculate?originLat=28.6139&originLon=77.2090&destLat=28.6542&destLon=77.2373
     * Calculate fare for a journey between two pin locations
     */
    @GetMapping("/fare/calculate")
    public ResponseEntity<FareInfo> calculateFareByLocation(
            @RequestParam double originLat,
            @RequestParam double originLon,
            @RequestParam double destLat,
            @RequestParam double destLon) {

        FareInfo fareInfo = transitService.calculateFareByLocation(originLat, originLon, destLat, destLon);
        return ResponseEntity.ok(fareInfo);
    }

    // ===============================
    // GET NEAREST STOP FROM PIN
    // ===============================
    /**
     * GET /api/transit/stops/nearest?lat=28.6139&lon=77.2090
     * Find the nearest stop to a pin location
     */
    @GetMapping("/stops/nearest")
    public ResponseEntity<Stop> getNearestStop(
            @RequestParam double lat,
            @RequestParam double lon) {

        Stop nearestStop = transitService.findNearestStop(lat, lon);

        if (nearestStop == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(nearestStop);
    }

    // ===============================
    // EXISTING ENDPOINTS (KEEP FOR BACKWARD COMPATIBILITY)
    // ===============================

    @GetMapping("/stops/{stopId}/next-buses")
    public ResponseEntity<List<NextBusDTO>> getNextBuses(
            @PathVariable String stopId,
            @RequestParam(defaultValue = "5") int limit) {

        List<NextBusDTO> nextBuses = transitService.getNextBuses(stopId, limit);
        return ResponseEntity.ok(nextBuses);
    }

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