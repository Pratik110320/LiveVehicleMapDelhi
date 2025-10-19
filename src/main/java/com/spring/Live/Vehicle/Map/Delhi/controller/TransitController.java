package com.spring.Live.Vehicle.Map.Delhi.controller;

import com.spring.Live.Vehicle.Map.Delhi.service.TransitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "${app.cors.allowed-origins}")
public class TransitController {

    @Autowired
    private TransitService transitService;

    @GetMapping("/api/routes")
    public List<Map<String, Object>> getRoutesByStopName(@RequestParam String stopName) {
        return transitService.getRoutesByStopName(stopName);
    }
}
