package com.spring.Live.Vehicle.Map.Delhi.controller;


import com.spring.Live.Vehicle.Map.Delhi.model.VehicleDto;
import com.spring.Live.Vehicle.Map.Delhi.service.VehicleStoreService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import java.time.Instant;
import java.util.List;


@RestController
@RequestMapping("/api")
public class VehiclesController {
    private final VehicleStoreService store;


    public VehiclesController(VehicleStoreService store) {
        this.store = store;
    }


    @GetMapping("/vehicles")
    public ResponseEntity<List<VehicleDto>> vehicles() {
        return ResponseEntity.ok(store.getAll());
    }


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


    @GetMapping("/vehicles/{id}")
    public ResponseEntity<VehicleDto> getVehicle(@PathVariable String id) {
// naive lookup from redis keys
        List<VehicleDto> all = store.getAll();
        return all.stream().filter(v -> id.equals(v.getVehicleId()) || id.equals(v.getTripId()))
                .findFirst().map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
}