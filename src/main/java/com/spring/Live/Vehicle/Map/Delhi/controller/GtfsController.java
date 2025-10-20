package com.spring.Live.Vehicle.Map.Delhi.controller;

import com.spring.Live.Vehicle.Map.Delhi.dto.RouteDTO;
import com.spring.Live.Vehicle.Map.Delhi.dto.StopDTO;
import com.spring.Live.Vehicle.Map.Delhi.model.Route;
import com.spring.Live.Vehicle.Map.Delhi.model.RouteDTO;
import com.spring.Live.Vehicle.Map.Delhi.model.Stop;
import com.spring.Live.Vehicle.Map.Delhi.model.StopDTO;
import com.spring.Live.Vehicle.Map.Delhi.service.GtfsStaticService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/gtfs")
public class GtfsController {

    private final GtfsStaticService gtfsStaticService;

    public GtfsController(GtfsStaticService gtfsStaticService) {
        this.gtfsStaticService = gtfsStaticService;
    }

    @GetMapping("/routes")
    public List<RouteDTO> getAllRoutes() {
        return gtfsStaticService.getAllRoutes().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/routes/{routeId}/stops")
    public List<StopDTO> getStopsByRoute(@PathVariable String routeId) {
        return gtfsStaticService.getStopsByRouteId(routeId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/stops")
    public List<StopDTO> getAllStops() {
        return gtfsStaticService.getAllStops().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // --- DTO Conversion Methods ---

    private RouteDTO convertToDto(Route route) {
        return new RouteDTO(
                route.getRouteId(),
                route.getAgencyId(),
                route.getRouteShortName(),
                route.getRouteLongName(),
                route.getRouteType()
        );
    }

    private StopDTO convertToDto(Stop stop) {
        return new StopDTO(
                stop.getStopId(),
                stop.getStopName(),
                stop.getStopLat(),
                stop.getStopLon()
        );
    }
}
