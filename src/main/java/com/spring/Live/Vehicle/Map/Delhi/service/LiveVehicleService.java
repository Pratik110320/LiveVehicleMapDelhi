package com.spring.Live.Vehicle.Map.Delhi.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.transit.realtime.GtfsRealtime;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class LiveVehicleService {

    private static final Logger logger = LoggerFactory.getLogger(LiveVehicleService.class);

    @Value("${otd.realtime.url}")
    private String otdRealtimeUrl;

    @Autowired
    private WebClient webClient;

    @Autowired
    private VehicleStoreService vehicleStoreService;

    @Autowired
    private NotificationService notificationService;

    private final Counter fetchSuccessCounter;
    private final Counter fetchFailureCounter;

    public LiveVehicleService(VehicleStoreService vehicleStoreService, MeterRegistry meterRegistry) {
        this.fetchSuccessCounter = Counter.builder("gtfs.fetch.success")
                .description("Counts successful GTFS-RT feed fetches")
                .register(meterRegistry);
        this.fetchFailureCounter = Counter.builder("gtfs.fetch.failure")
                .description("Counts failed GTFS-RT feed fetches")
                .register(meterRegistry);

        // This gauge will report the current number of active vehicles
        meterRegistry.gauge("gtfs.vehicles.active", vehicleStoreService, VehicleStoreService::getActiveVehicleCount);
    }


    @Scheduled(fixedRate = 10000) // 10 seconds
    public void fetchAndProcessVehicleData() {
        try {
            logger.info("Fetching live vehicle data...");
            GtfsRealtime.FeedMessage feed = GtfsRealtime.FeedMessage.parseFrom(new URL(otdRealtimeUrl).openStream());

            int updatedCount = vehicleStoreService.updateVehicles(feed);
            logger.info("Successfully processed and updated {} vehicles.", updatedCount);

            // Notify SSE clients with the latest vehicle data
            List<Map<String, Object>> vehicleList = vehicleStoreService.getAllVehicles().values().stream()
                    .map(v -> {
                        // Using HashMap to ensure the value type is Object, resolving the type conflict.
                        Map<String, Object> map = new HashMap<>();
                        map.put("id", v.getVehicleId());
                        map.put("latitude", v.getLat());
                        map.put("longitude", v.getLon());
                        map.put("routeId", v.getRouteId() != null ? v.getRouteId() : "");
                        return map;
                    })
                    .collect(Collectors.toList());

            try {
                notificationService.sendNotification(new ObjectMapper().writeValueAsString(vehicleList));
            } catch (JsonProcessingException e) {
                logger.error("Failed to serialize vehicle data for SSE notification", e);
            }

            fetchSuccessCounter.increment();

        } catch (IOException e) {
            logger.error("Error fetching or parsing GTFS-RT data", e);
            fetchFailureCounter.increment();
        }
    }
}

