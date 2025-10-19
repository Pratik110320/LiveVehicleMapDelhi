package com.spring.Live.Vehicle.Map.Delhi.model;

import java.util.Map;
import java.util.Set;

public class VehicleUpdateDelta {

    private final Set<String> added;
    private final Set<String> updated;
    private final Set<String> removed;
    private final Map<String, Vehicle> currentVehicles;

    public VehicleUpdateDelta(Set<String> added, Set<String> updated, Set<String> removed, Map<String, Vehicle> currentVehicles) {
        this.added = added;
        this.updated = updated;
        this.removed = removed;
        this.currentVehicles = currentVehicles;
    }

    public Set<String> getAdded() {
        return added;
    }

    public Set<String> getUpdated() {
        return updated;
    }

    public Set<String> getRemoved() {
        return removed;
    }

    public Map<String, Vehicle> getCurrentVehicles() {
        return currentVehicles;
    }
}
