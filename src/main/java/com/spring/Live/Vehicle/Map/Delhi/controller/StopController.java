package com.spring.Live.Vehicle.Map.Delhi.controller;

import com.spring.Live.Vehicle.Map.Delhi.model.StopDto;
import com.spring.Live.Vehicle.Map.Delhi.model.StopTimeDto;
import com.spring.Live.Vehicle.Map.Delhi.service.StopService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class StopController {
    private final StopService stopService;

    public StopController(StopService stopService) {
        this.stopService = stopService;
    }

    @GetMapping("/stops/{stop_id}")
    public ResponseEntity<StopDto> getStop(@PathVariable("stop_id") String stopId) {
        StopDto s = stopService.getStop(stopId);
        if (s == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(s);
    }

    @GetMapping("/stops/{stop_id}/arrivals")
    public ResponseEntity<List<StopTimeDto>> getArrivals(
            @PathVariable("stop_id") String stopId,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        return ResponseEntity.ok(stopService.getArrivals(stopId, from, to));
    }
}
