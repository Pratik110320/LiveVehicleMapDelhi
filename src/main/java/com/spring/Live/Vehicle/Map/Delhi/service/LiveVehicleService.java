package com.spring.Live.Vehicle.Map.Delhi.service;

import com.google.transit.realtime.GtfsRealtime;
import com.spring.Live.Vehicle.Map.Delhi.model.Vehicle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.net.URL;

@Service
public class LiveVehicleService {

    private static final Logger logger = LoggerFactory.getLogger(LiveVehicleService.class);

    @Value("${otd.realtime.url}")
    private String gtfsRtUrl;

    private final WebClient webClient;
    private final VehicleStoreService vehicleStoreService;
    private final NotificationService notificationService;

    @Autowired
    public LiveVehicleService(WebClient webClient, VehicleStoreService vehicleStoreService, NotificationService notificationService) {
        this.webClient = webClient;
        this.vehicleStoreService = vehicleStoreService;
        this.notificationService = notificationService;
    }


    @Scheduled(fixedRate = 10000)
    public void fetchAndProcessVehicleData() {
        logger.info("Fetching real-time vehicle data from URL: {}", gtfsRtUrl);
        try {
            URL url = new URL(gtfsRtUrl);
            GtfsRealtime.FeedMessage feedMessage = GtfsRealtime.FeedMessage.parseFrom(url.openStream());

            int updatedCount = 0;
            for (GtfsRealtime.FeedEntity entity : feedMessage.getEntityList()) {
                if (entity.hasVehicle()) {
                    GtfsRealtime.VehiclePosition vehiclePosition = entity.getVehicle();
                    Vehicle vehicle = new Vehicle(
                            vehiclePosition.getVehicle().getId(),   // vehicleId
                            vehiclePosition.getPosition().getLatitude(),
                            vehiclePosition.getPosition().getLongitude(),
                            vehiclePosition.getTrip().getRouteId(),
                            vehiclePosition.getTrip().getTripId(),
                            vehiclePosition.getTimestamp()
                    );


                    // Store the updated vehicle data in Redis
                    vehicleStoreService.addOrUpdateVehicle(vehicle);
                    updatedCount++;
                }
            }
            logger.info("Successfully processed and updated {} vehicles.", updatedCount);

            // Notify SSE clients with the full updated list
            notificationService.sendVehicleUpdates(vehicleStoreService.getAllVehicles());

        } catch (IOException e) {
            logger.error("Error fetching or parsing GTFS-RT data.", e);
        } catch (Exception e) {
            logger.error("An unexpected error occurred during vehicle data processing.", e);
        }
    }
}
