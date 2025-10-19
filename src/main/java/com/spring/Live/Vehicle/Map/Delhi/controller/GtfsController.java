package com.spring.Live.Vehicle.Map.Delhi.controller;

import com.spring.Live.Vehicle.Map.Delhi.model.Route;
import com.spring.Live.Vehicle.Map.Delhi.model.Stop;
import com.spring.Live.Vehicle.Map.Delhi.service.GtfsStaticService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/gtfs")
public class GtfsController {

    @Autowired
    private GtfsStaticService gtfsStaticService;

    @GetMapping("/stops")
    public List<Stop> getAllStops() {
        return gtfsStaticService.getAllStops();
    }

    @GetMapping("/routes")
    public List<Route> getAllRoutes() {
        return gtfsStaticService.getAllRoutes();
    }

    @GetMapping("/routes/search")
    public List<Route> searchRoutes(@RequestParam String query) {
        return gtfsStaticService.searchRoutes(query);
    }

    /**
     * New endpoint to get the upcoming bus schedule for a specific stop.
     * @param stopId The ID of the stop to get the schedule for.
     * @return A list of schedule entries, each containing the route name and arrival time.
     */
    @GetMapping("/stops/{stopId}/schedule")
    public ResponseEntity<List<Map<String, String>>> getStopSchedule(@PathVariable String stopId) {
        List<Map<String, String>> schedule = gtfsStaticService.getScheduleForStop(stopId);
        if (schedule.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(schedule);
    }
}

