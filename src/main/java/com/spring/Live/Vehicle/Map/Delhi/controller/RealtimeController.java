package com.spring.Live.Vehicle.Map.Delhi.controller;


import com.spring.Live.Vehicle.Map.Delhi.model.VehiclePosition;
import com.spring.Live.Vehicle.Map.Delhi.service.GtfsRealtimeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/realtime")
public class RealtimeController {
    private final GtfsRealtimeService gtfsService;

    public RealtimeController(GtfsRealtimeService gtfsService) {
        this.gtfsService = gtfsService;
    }

    @GetMapping("/VehiclePositions")
    public ResponseEntity<List<VehiclePosition>> getRealtimeVehicles() throws IOException {
        List<VehiclePosition> list = gtfsService.fetchVehiclePositions();
        return ResponseEntity.ok(list);
    }
}
