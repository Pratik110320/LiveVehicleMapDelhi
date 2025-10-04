package com.spring.Live.Vehicle.Map.Delhi.controller;

import com.spring.Live.Vehicle.Map.Delhi.model.ScheduleItemDto;
import com.spring.Live.Vehicle.Map.Delhi.model.TripDto;
import com.spring.Live.Vehicle.Map.Delhi.service.GtfsStaticService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class TripController {
    private final GtfsStaticService gtfsStaticService;

    public TripController(GtfsStaticService gtfsStaticService) {
        this.gtfsStaticService = gtfsStaticService;
    }

    @GetMapping("/trips/{trip_id}")
    public ResponseEntity<TripDto> getTrip(@PathVariable("trip_id") String tripId) {
        return ResponseEntity.of(Optional.ofNullable(gtfsStaticService.getTripById(tripId)));
    }

    @GetMapping("/trips/{trip_id}/stop-times")
    public ResponseEntity<List<ScheduleItemDto>> getStopTimes(@PathVariable("trip_id") String tripId) {
        // Returning ScheduleItemDto is more useful for the UI than StopTimeDto
        return ResponseEntity.ok(gtfsStaticService.getScheduleForTrip(tripId));
    }
}
