package com.spring.Live.Vehicle.Map.Delhi.controller;

import com.spring.Live.Vehicle.Map.Delhi.model.Vehicle;
import com.spring.Live.Vehicle.Map.Delhi.service.VehicleStoreService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@RestController
@RequestMapping("/api/vehicles")
public class VehiclesController {

    private final VehicleStoreService vehicleStoreService;

    public VehiclesController(VehicleStoreService vehicleStoreService) {
        this.vehicleStoreService = vehicleStoreService;
    }

    @GetMapping
    public Collection<Vehicle> getAllVehicles() {
        return vehicleStoreService.getAllVehicles();
    }
}
