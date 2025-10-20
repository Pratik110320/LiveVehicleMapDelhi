package com.spring.Live.Vehicle.Map.Delhi.model;

// Using record for immutable DTO
public record RouteDTO(
        String routeId,
        String agencyId,
        String routeShortName,
        String routeLongName,
        int routeType
) {}
