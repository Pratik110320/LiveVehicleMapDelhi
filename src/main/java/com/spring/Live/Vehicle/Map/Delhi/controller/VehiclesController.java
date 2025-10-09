package com.spring.Live.Vehicle.Map.Delhi.controller;

import com.spring.Live.Vehicle.Map.Delhi.model.*;
import com.spring.Live.Vehicle.Map.Delhi.service.VehicleStoreService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
class VehiclesController {

    private final VehicleStoreService store;

    public VehiclesController(VehicleStoreService store) {
        this.store = store;
    }

    @GetMapping("/status")
    public ResponseEntity<?> status() {
        Long ts = store.getLastFeedTs();
        long age = ts == null ? -1 : Instant.now().getEpochSecond() - ts;
        return ResponseEntity.ok(Map.of(
                "feedUrlSet", true,
                "lastFeedTimestamp", ts,
                "feedAgeSeconds", age,
                "activeVehicleCount", store.getActiveVehicleCount()
        ));
    }

    @GetMapping("/vehicles/{id}")
    public ResponseEntity<Vehicle> getVehicle(@PathVariable String id) {
        return store.getById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/vehicles")
    public ResponseEntity<List<Vehicle>> getAllVehicles() {
        return ResponseEntity.ok(store.getAllVehicles().values().stream().toList());
    }
}