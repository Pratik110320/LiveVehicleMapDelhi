package com.spring.Live.Vehicle.Map.Delhi.service;

import com.spring.Live.Vehicle.Map.Delhi.model.Vehicle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class VehicleStoreService {

    private static final Logger logger = LoggerFactory.getLogger(VehicleStoreService.class);
    private final ConcurrentHashMap<String, Vehicle> vehicleMap = new ConcurrentHashMap<>();
    private static final long STALE_VEHICLE_THRESHOLD_SECONDS = 300; // 5 minutes

    /**
     * Add or update a vehicle and set the last updated timestamp.
     */
    public void addOrUpdateVehicle(Vehicle vehicle) {
        if (vehicle != null && vehicle.getVehicleId() != null) {
            vehicle.setLastUpdated(Instant.now().getEpochSecond());
            vehicleMap.put(vehicle.getVehicleId(), vehicle);
            logger.debug("Added/Updated vehicle: {}", vehicle.getVehicleId());
        } else {
            logger.warn("Invalid vehicle data; ID is missing.");
        }
    }

    /**
     * Retrieve all vehicles as a Map.
     */
    public Map<String, Vehicle> getAllVehicles() {
        return new ConcurrentHashMap<>(vehicleMap);
    }

    /**
     * Retrieve a vehicle by ID.
     */
    public Optional<Vehicle> getById(String vehicleId) {
        return Optional.ofNullable(vehicleMap.get(vehicleId));
    }

    /**
     * Get the count of active vehicles.
     */
    public int getActiveVehicleCount() {
        return vehicleMap.size();
    }

    /**
     * Scheduled cleanup of stale vehicles.
     */
    @Scheduled(fixedRate = 60000) // every 1 minute
    public void removeStaleVehicles() {
        long currentTime = Instant.now().getEpochSecond();
        long staleThreshold = currentTime - STALE_VEHICLE_THRESHOLD_SECONDS;

        var staleIds = vehicleMap.entrySet().stream()
                .filter(entry -> entry.getValue().getLastUpdated() < staleThreshold)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

        if (!staleIds.isEmpty()) {
            staleIds.forEach(vehicleMap::remove);
            logger.info("Removed {} stale vehicles. {} remaining.", staleIds.size(), vehicleMap.size());
        }
    }

    /**
     * Get the last feed timestamp (most recent vehicle update).
     */
    public Long getLastFeedTs() {
        return vehicleMap.values().stream()
                .map(Vehicle::getLastUpdated)
                .max(Long::compareTo)
                .orElse(null);
    }
}
