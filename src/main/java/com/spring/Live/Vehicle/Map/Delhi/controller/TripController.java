package com.spring.Live.Vehicle.Map.Delhi.controller;

import com.spring.Live.Vehicle.Map.Delhi.model.StopTimeDto;
import com.spring.Live.Vehicle.Map.Delhi.model.TripDto;
import com.spring.Live.Vehicle.Map.Delhi.service.TripService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class TripController {
    private final TripService tripService;

    public TripController(TripService tripService) {
        this.tripService = tripService;
    }

    @GetMapping("/trips/{trip_id}")
    public ResponseEntity<TripDto> getTrip(@PathVariable("trip_id") String tripId) {
        TripDto t = tripService.getTrip(tripId);
        if (t == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(t);
    }

    @GetMapping("/trips/{trip_id}/stop-times")
    public ResponseEntity<List<StopTimeDto>> getStopTimes(@PathVariable("trip_id") String tripId) {
        return ResponseEntity.ok(tripService.getStopTimesForTrip(tripId));
    }
}
