package com.spring.Live.Vehicle.Map.Delhi.health;
import com.spring.Live.Vehicle.Map.Delhi.service.VehicleStoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.time.Instant;
@Component
public class GtfsFeedHealthIndicator implements HealthIndicator {

    @Autowired
    private VehicleStoreService vehicleStore;

    @Override
    public Health health() {
        Long lastUpdate = vehicleStore.getLastFeedTs();
        if (lastUpdate == null) {
            return Health.down().withDetail("reason", "No data received yet").build();
        }

        long age = Instant.now().getEpochSecond() - lastUpdate;
        // If data is older than 5 minutes (300 seconds), consider it down.
        if (age > 300) {
            return Health.down()
                    .withDetail("reason", "GTFS-RT feed is stale")
                    .withDetail("lastUpdateTimestamp", lastUpdate)
                    .withDetail("ageInSeconds", age)
                    .build();
        }

        return Health.up()
                .withDetail("activeVehicles", vehicleStore.getActiveVehicleCount())
                .withDetail("lastUpdateTimestamp", lastUpdate)
                .withDetail("ageInSeconds", age)
                .build();
    }
}



