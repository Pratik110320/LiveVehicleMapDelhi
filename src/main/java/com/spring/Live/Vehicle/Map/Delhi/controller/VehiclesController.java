package com.spring.Live.Vehicle.Map.Delhi.controller;

import com.spring.Live.Vehicle.Map.Delhi.model.*;
import com.spring.Live.Vehicle.Map.Delhi.service.VehicleStoreService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

/**
 * Controller for status checks and fetching individual vehicle data.
 * Real-time updates are handled by SseController.
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") // Allow all origins for simplicity
public class VehiclesController {
    private final VehicleStoreService store;

    public VehiclesController(VehicleStoreService store) {
        this.store = store;
    }

    /**
     * Provides a status check of the application's real-time feed.
     * @return A map containing the timestamp of the last feed and its age.
     */
    @GetMapping("/status")
    public ResponseEntity<?> status() {
        Long ts = store.getLastFeedTs();
        long age = ts == null ? -1 : Instant.now().getEpochSecond() - ts;
        return ResponseEntity.ok(
                java.util.Map.of(
                        "feedUrlSet", true,
                        "lastFeedTimestamp", ts,
                        "feedAgeSeconds", age
                )
        );
    }

    /**
     * Retrieves the last known position of a single vehicle by its ID.
     * @param id The vehicle ID to look up.
     * @return A single VehicleDto or 404 if not found.
     */
    @GetMapping("/vehicles/{id}")
    public ResponseEntity<VehicleDto> getVehicle(@PathVariable String id) {
        return store.getById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
