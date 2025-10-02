package com.spring.Live.Vehicle.Map.Delhi.gtfs;


import com.google.transit.realtime.GtfsRealtime;
import com.spring.Live.Vehicle.Map.Delhi.model.VehicleDto;
import com.spring.Live.Vehicle.Map.Delhi.service.GtfsStaticService;
import com.spring.Live.Vehicle.Map.Delhi.service.VehicleStoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;


import java.time.Instant;
import java.util.List;


@Component
@EnableScheduling
public class GtfsRtPoller {
    private final Logger log = LoggerFactory.getLogger(GtfsRtPoller.class);
    private final VehicleStoreService store;
    private final WebClient webClient;
    private final GtfsStaticService staticService; // Service to access static data

    @Value("${otd.realtime.url}")
    private String feedUrl;

    public GtfsRtPoller(VehicleStoreService store, WebClient webClient, GtfsStaticService staticService) {
        this.store = store;
        this.webClient = webClient;
        this.staticService = staticService;
    }

    @Scheduled(fixedDelayString = "${otd.realtime.poll.ms}")
    public void poll() {
        log.info("Polling GTFS-RT feed...");
        try {
            byte[] data = webClient.get()
                    .uri(feedUrl)
                    .retrieve()
                    .bodyToMono(byte[].class)
                    .block();

            if (data == null || data.length == 0) {
                log.warn("Empty feed bytes received from API");
                return;
            }

            GtfsRealtime.FeedMessage feed = GtfsRealtime.FeedMessage.parseFrom(data);
            log.info("Feed parsed successfully. Found {} entities.", feed.getEntityCount());
            int processedVehicleCount = 0;
            int totalVehicleCount = 0;

            for (GtfsRealtime.FeedEntity entity : feed.getEntityList()) {
                if (entity.hasVehicle()) {
                    totalVehicleCount++;
                    GtfsRealtime.VehiclePosition vp = entity.getVehicle();
                    VehicleDto v = new VehicleDto();

                    String vehicleId = vp.hasVehicle() && vp.getVehicle().hasId() ? vp.getVehicle().getId() : null;
                    v.setVehicleId(vehicleId);

                    String tripId = null;
                    String routeId = null;

                    if (vp.hasTrip()) {
                        GtfsRealtime.TripDescriptor trip = vp.getTrip();
                        tripId = trip.hasTripId() ? trip.getTripId() : null;
                        routeId = trip.hasRouteId() ? trip.getRouteId() : null;
                    }
                    v.setTripId(tripId);

                    if (routeId == null && tripId != null) {
                        routeId = staticService.getRouteIdForTrip(tripId);
                    }

                    if (routeId != null) {
                        v.setRouteId(routeId);
                        String routeName = staticService.getRouteNameForRoute(routeId);
                        v.setRouteName(routeName != null && !routeName.isBlank() ? routeName : "Route " + routeId);
                    }

                    v.setLat(vp.hasPosition() ? vp.getPosition().getLatitude() : 0.0);
                    v.setLon(vp.hasPosition() ? vp.getPosition().getLongitude() : 0.0);
                    v.setSpeed(vp.hasPosition() ? vp.getPosition().getSpeed() : 0.0);
                    v.setTimestamp(vp.hasTimestamp() ? vp.getTimestamp() : Instant.now().getEpochSecond());

                    // ** NEW: Validation step before saving the vehicle **
                    // We check if the tripId from the live feed actually exists in our static stop_times data.
                    // If it doesn't, we skip this vehicle, as we won't be able to show its stops.
                    if (tripId != null && !staticService.getStopTimesForTrip(tripId).isEmpty()) {
                        store.save(v);
                        processedVehicleCount++;
                    } else {
                        log.debug("Skipping vehicle {} on trip {} as it has no matching static data.", vehicleId, tripId);
                    }
                }
            }
            log.info("Finished processing. Total vehicles in feed: {}. Validated and stored: {}.", totalVehicleCount, processedVehicleCount);
        } catch (Exception e) {
            log.error("Error polling GTFS-RT feed", e);
        }
    }
}
