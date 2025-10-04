package com.spring.Live.Vehicle.Map.Delhi.controller;


import com.spring.Live.Vehicle.Map.Delhi.model.RouteDto;
import com.spring.Live.Vehicle.Map.Delhi.service.RouteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api")
public class RouteController {
    private final RouteService routeService;

    public RouteController(RouteService routeService) { this.routeService = routeService; }

    @GetMapping("/routes")
    public ResponseEntity<Collection<RouteDto>> listRoutes(@RequestParam(required = false) String q) {
        Collection<RouteDto> routes = routeService.listAllRoutes();
        if (q != null && !q.isEmpty()) {
            String qlc = q.toLowerCase();
            List<RouteDto> filtered = routes.stream()
                    .filter(r -> (r.getRouteLongName() != null && r.getRouteLongName().toLowerCase().contains(qlc)) ||
                            (r.getRouteShortName() != null && r.getRouteShortName().toLowerCase().contains(qlc)))
                    .toList();
            return ResponseEntity.ok(filtered);
        }
        return ResponseEntity.ok(routes);
    }

    @GetMapping("/routes/{route_id}")
    public ResponseEntity<?> getRoute(@PathVariable("route_id") String routeId) {
        RouteDto route = routeService.getRoute(routeId);
        if (route == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(route); // route contains trips because loader attached them
    }
}

