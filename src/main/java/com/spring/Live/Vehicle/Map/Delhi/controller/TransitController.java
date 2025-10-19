package com.spring.Live.Vehicle.Map.Delhi.controller;

import com.spring.Live.Vehicle.Map.Delhi.model.Stop;
import com.spring.Live.Vehicle.Map.Delhi.repository.StopRepository;
import com.spring.Live.Vehicle.Map.Delhi.service.NotificationService;
import com.spring.Live.Vehicle.Map.Delhi.service.TransitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/transit")
public class TransitController {

    @Autowired
    private TransitService transitService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private StopRepository stopRepository; // Inject StopRepository

    @GetMapping("/route")
    public ResponseEntity<List<Stop>> findRoute(@RequestParam String from, @RequestParam String to) {
        // Find the full Stop objects from the database using their names.
        Optional<Stop> fromStopOpt = stopRepository.findByStopName(from);
        Optional<Stop> toStopOpt = stopRepository.findByStopName(to);

        // If either stop cannot be found, return a "Not Found" response.
        if (fromStopOpt.isEmpty() || toStopOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Call the service with the found Stop objects.
        List<Stop> route = transitService.findRoute(fromStopOpt.get(), toStopOpt.get());

        if (route.isEmpty()) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        return ResponseEntity.ok(route);
    }


    @PostMapping("/notifications/subscribe")
    public void subscribeToNotifications(@RequestBody Map<String, String> payload) {
        String vehicleId = payload.get("vehicleId");
        notificationService.subscribe(vehicleId);
    }
}
