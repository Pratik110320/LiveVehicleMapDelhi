package com.spring.Live.Vehicle.Map.Delhi.controller;

import com.spring.Live.Vehicle.Map.Delhi.model.FareAttributeDto;
import com.spring.Live.Vehicle.Map.Delhi.service.GtfsStaticService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api")
public class FareController {

    private final GtfsStaticService gtfsStaticService;

    public FareController(GtfsStaticService gtfsStaticService) {
        this.gtfsStaticService = gtfsStaticService;
    }

    // This endpoint is not used by the current UI, but kept for completeness.
    @GetMapping("/fares/route/{route_id}")
    public ResponseEntity<?> getFaresForRoute(@PathVariable("route_id") String routeId) {
        // This method would require implementation in GtfsStaticService if needed.
        // For now, it returns "not implemented" to avoid confusion.
        return ResponseEntity.status(501).body("Not Implemented");
    }

    // This endpoint is used by the UI's new Fare Calculator.
    @GetMapping("/fares")
    public ResponseEntity<FareAttributeDto> getFareForOriginDest(
            @RequestParam String origin,
            @RequestParam String destination,
            @RequestParam String routeId
    ) {
        return ResponseEntity.of(Optional.ofNullable(
                gtfsStaticService.getFare(routeId, origin, destination))
        );
    }
}
