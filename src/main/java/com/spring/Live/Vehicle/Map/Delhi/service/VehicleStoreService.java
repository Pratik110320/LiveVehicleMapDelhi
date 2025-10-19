package com.spring.Live.Vehicle.Map.Delhi.service;

import com.google.transit.realtime.GtfsRealtime;
import com.spring.Live.Vehicle.Map.Delhi.model.Vehicle;
import com.spring.Live.Vehicle.Map.Delhi.model.VehicleUpdateDelta;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class VehicleStoreService {

    private final Map<String, Vehicle> vehicles = new ConcurrentHashMap<>();
    private final Map<String, Vehicle> previousSnapshot = new ConcurrentHashMap<>();
    private Long lastFeedTs = null;
    private static final long STALE_VEHICLE_THRESHOLD_SECONDS = 300; // 5 minutes

    public int updateVehicles(GtfsRealtime.FeedMessage feedMessage) {
        Set<String> updatedVehicleIds = new HashSet<>();

        feedMessage.getEntityList().forEach(entity -> {
            if (entity.hasVehicle()) {
                GtfsRealtime.VehiclePosition vp = entity.getVehicle();
                Vehicle vehicle = new Vehicle();
                vehicle.setVehicleId(vp.getVehicle().getId());
                vehicle.setLat(vp.getPosition().getLatitude());
                vehicle.setLon(vp.getPosition().getLongitude());
                vehicle.setRouteId(vp.getTrip().getRouteId());
                vehicle.setLastUpdated(Instant.now().getEpochSecond());

                vehicles.put(vehicle.getVehicleId(), vehicle);
                updatedVehicleIds.add(vehicle.getVehicleId());
            }
        });
        this.lastFeedTs = Instant.now().getEpochSecond();
        removeStaleVehicles(updatedVehicleIds);
        return updatedVehicleIds.size();
    }

    private void removeStaleVehicles(Set<String> currentVehicleIds) {
        long now = Instant.now().getEpochSecond();
        vehicles.entrySet().removeIf(entry ->
                !currentVehicleIds.contains(entry.getKey()) &&
                        (now - entry.getValue().getLastUpdated()) > STALE_VEHICLE_THRESHOLD_SECONDS);
    }

    public Map<String, Vehicle> getAllVehicles() {
        return new ConcurrentHashMap<>(vehicles);
    }

    public VehicleUpdateDelta getUpdatesSince() {
        Map<String, Vehicle> current = getAllVehicles();
        Set<String> added = new HashSet<>();
        Set<String> updated = new HashSet<>();
        // Start with all previous keys, and remove the ones we still see
        Set<String> removed = new HashSet<>(previousSnapshot.keySet());

        current.forEach((id, vehicle) -> {
            removed.remove(id); // If it's in current, it's not removed
            if (!previousSnapshot.containsKey(id)) {
                added.add(id); // New vehicle
            } else if (!vehicle.equals(previousSnapshot.get(id))) {
                updated.add(id); // Existing vehicle with changes
            }
        });

        previousSnapshot.clear();
        previousSnapshot.putAll(current);

        return new VehicleUpdateDelta(added, updated, removed, current);
    }

    public Long getLastFeedTs() {
        return lastFeedTs;
    }

    public int getActiveVehicleCount() {
        return vehicles.size();
    }
}
