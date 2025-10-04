package com.spring.Live.Vehicle.Map.Delhi.service;


import com.spring.Live.Vehicle.Map.Delhi.model.CsvDataLoader;
import com.spring.Live.Vehicle.Map.Delhi.model.RouteDto;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RouteService {
    private final CsvDataLoader loader;

    public RouteService(CsvDataLoader loader) { this.loader = loader; }

    public Collection<RouteDto> listAllRoutes() { return loader.getAllRoutes(); }

    public RouteDto getRoute(String routeId) { return loader.getRouteById(routeId); }
}

