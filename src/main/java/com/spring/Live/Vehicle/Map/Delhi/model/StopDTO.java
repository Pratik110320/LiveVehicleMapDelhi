package com.spring.Live.Vehicle.Map.Delhi.model;

// Using record for immutable DTO
public record StopDTO(
        String stopId,
        String stopName,
        double stopLat,
        double stopLon
) {}
