package com.spring.Live.Vehicle.Map.Delhi.controller;

import com.spring.Live.Vehicle.Map.Delhi.model.Route;
import com.spring.Live.Vehicle.Map.Delhi.model.Stop;
import com.spring.Live.Vehicle.Map.Delhi.service.GtfsStaticService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/gtfs")
@CrossOrigin(origins = "${app.cors.allowed-origins}")
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

    @GetMapping("/schedule")
    public List<Map<String, String>> getScheduleForStop(@RequestParam String stopId) {
        return gtfsStaticService.getScheduleForStop(stopId);
    }
}
