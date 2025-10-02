package com.spring.Live.Vehicle.Map.Delhi.controller;

import com.spring.Live.Vehicle.Map.Delhi.model.FareAttributeDto;
import com.spring.Live.Vehicle.Map.Delhi.model.RouteDto;
import com.spring.Live.Vehicle.Map.Delhi.model.StopDto;
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

    // Endpoint to get all routes
    @GetMapping("/routes")
    public Collection<RouteDto> getAllRoutes() {
        return gtfsStaticService.getAllRoutes();
    }

    // Endpoint to get all stops for a specific route
    @GetMapping("/routes/{routeId}/stops")
    public List<StopDto> getStopsByRoute(@PathVariable String routeId) {
        return gtfsStaticService.getStopsByRouteId(routeId);
    }

    // Endpoint to search for stops by name
    @GetMapping("/stops/search")
    public List<StopDto> searchStops(@RequestParam("q") String query) {
        return gtfsStaticService.searchStopsByName(query);
    }

    // Endpoint to calculate the fare for a trip segment
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

