package com.spring.Live.Vehicle.Map.Delhi.service;

import com.google.transit.realtime.GtfsRealtime;
import com.spring.Live.Vehicle.Map.Delhi.model.Vehicle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LiveVehicleService {

    private static final Logger log = LoggerFactory.getLogger(LiveVehicleService.class);

    private final WebClient webClient;
    private final VehicleStoreService vehicleStoreService;
    private final NotificationService notificationService;

    @Value("${gtfs.realtime.url}")
    private String gtfsRealtimeUrl;

    public LiveVehicleService(WebClient webClient, VehicleStoreService vehicleStoreService, NotificationService notificationService) {
        this.webClient = webClient;
        this.vehicleStoreService = vehicleStoreService;
        this.notificationService = notificationService;
    }

    @Scheduled(fixedRateString = "${app.scheduling.vehicle-fetch-rate}")
    public void fetchAndProcessLiveVehicleData() {
        log.info("Fetching live vehicle data from: {}", gtfsRealtimeUrl);
        try {
            byte[] feedBytes = webClient.get()
                    .uri(gtfsRealtimeUrl)
                    .retrieve()
                    .bodyToMono(byte[].class)
                    .block();

            if (feedBytes == null || feedBytes.length == 0) {
                log.warn("GTFS-RT feed is empty or null.");
                return;
            }

            GtfsRealtime.FeedMessage feedMessage = GtfsRealtime.FeedMessage.parseFrom(feedBytes);
            List<Vehicle> currentVehicles = feedMessage.getEntityList().stream()
                    .filter(GtfsRealtime.FeedEntity::hasVehicle)
                    .map(entity -> {
                        GtfsRealtime.VehiclePosition vehiclePosition = entity.getVehicle();
                        Vehicle vehicle = new Vehicle();
                        vehicle.setId(entity.getId());
                        vehicle.setTripId(vehiclePosition.getTrip().getTripId());
                        vehicle.setRouteId(vehiclePosition.getTrip().getRouteId());
                        vehicle.setLatitude(vehiclePosition.getPosition().getLatitude());
                        vehicle.setLongitude(vehiclePosition.getPosition().getLongitude());
                        vehicle.setBearing(vehiclePosition.getPosition().getBearing());
                        vehicle.setSpeed(vehiclePosition.getPosition().getSpeed());
                        vehicle.setTimestamp(vehiclePosition.getTimestamp());
                        vehicle.setVehicleId(vehiclePosition.getVehicle().getId());
                        return vehicle;
                    })
                    .collect(Collectors.toList());

            log.info("Fetched {} live vehicle positions.", currentVehicles.size());
            vehicleStoreService.updateVehicles(currentVehicles);
            notificationService.notifyVehicleUpdates(currentVehicles);

        } catch (Exception e) {
            log.error("Error fetching or processing GTFS-RT feed", e);
        }
    }
}
