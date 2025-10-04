package com.spring.Live.Vehicle.Map.Delhi.controller;

import com.spring.Live.Vehicle.Map.Delhi.model.StopDto;
import com.spring.Live.Vehicle.Map.Delhi.model.StopTimeDto;
import com.spring.Live.Vehicle.Map.Delhi.service.GtfsStaticService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class StopController {
    private final GtfsStaticService gtfsStaticService;

    public StopController(GtfsStaticService gtfsStaticService) {
        this.gtfsStaticService = gtfsStaticService;
    }

    @GetMapping("/stops/{stop_id}")
    public ResponseEntity<StopDto> getStop(@PathVariable("stop_id") String stopId) {
        return ResponseEntity.of(Optional.ofNullable(gtfsStaticService.getStopById(stopId)));
    }

    @GetMapping("/stops/{stop_id}/arrivals")
    public ResponseEntity<List<StopTimeDto>> getArrivals(
            @PathVariable("stop_id") String stopId,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        return ResponseEntity.ok(gtfsStaticService.getArrivals(stopId, from, to));
    }
}
