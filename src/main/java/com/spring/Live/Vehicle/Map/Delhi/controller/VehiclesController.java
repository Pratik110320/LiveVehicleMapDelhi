package com.spring.Live.Vehicle.Map.Delhi.controller;

import com.spring.Live.Vehicle.Map.Delhi.model.Vehicle;
import com.spring.Live.Vehicle.Map.Delhi.service.VehicleStoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/vehicles")
@CrossOrigin(origins = "${app.cors.allowed-origins}")
public class VehiclesController {

    @Autowired
    private VehicleStoreService vehicleStoreService;

    @GetMapping("/all")
    public Map<String, Vehicle> getAllVehicles() {
        return vehicleStoreService.getAllVehicles();
    }
}
