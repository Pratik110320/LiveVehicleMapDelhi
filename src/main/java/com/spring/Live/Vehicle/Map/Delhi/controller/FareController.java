package com.spring.Live.Vehicle.Map.Delhi.controller;

import com.spring.Live.Vehicle.Map.Delhi.model.FareAttributeDto;
import com.spring.Live.Vehicle.Map.Delhi.service.FareService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class FareController {

    @Autowired
    private final FareService fareService;

    public FareController(FareService fareService) {
        this.fareService = fareService;
    }

    @GetMapping("/fares/route/{route_id}")
    public ResponseEntity<List<FareAttributeDto>> getFaresForRoute(@PathVariable("route_id") String routeId) {
        return ResponseEntity.ok(fareService.getFaresForRoute(routeId));
    }

    @GetMapping("/fares")
    public ResponseEntity<?> getFareForOriginDest(@RequestParam String origin, @RequestParam String destination,@RequestParam String routeId) {
        return fareService.getFareForOriginDest(origin, destination,routeId)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
