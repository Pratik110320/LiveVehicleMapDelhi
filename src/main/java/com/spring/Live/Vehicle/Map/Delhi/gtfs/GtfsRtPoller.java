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
            int vehicleCount = 0;

            for (GtfsRealtime.FeedEntity entity : feed.getEntityList()) {
                if (entity.hasVehicle()) {
                    vehicleCount++;
                    GtfsRealtime.VehiclePosition vp = entity.getVehicle();
                    VehicleDto v = new VehicleDto();

                    String vehicleId = vp.hasVehicle() && vp.getVehicle().hasId() ? vp.getVehicle().getId() : null;
                    v.setVehicleId(vehicleId);

                    // ** NEW: Improved logic to find route information **
                    String tripId = null;
                    String routeId = null;

                    if (vp.hasTrip()) {
                        GtfsRealtime.TripDescriptor trip = vp.getTrip();
                        tripId = trip.hasTripId() ? trip.getTripId() : null;
                        // Prioritize route_id from the realtime feed if it exists, as trip_id may not match static data
                        routeId = trip.hasRouteId() ? trip.getRouteId() : null;
                    }
                    v.setTripId(tripId);

                    // If routeId was not in the realtime feed, fall back to looking it up from static data using tripId
                    if (routeId == null && tripId != null) {
                        routeId = staticService.getRouteIdForTrip(tripId);
                    }

                    // Now that we have the best possible routeId, find the route name
                    if (routeId != null) {
                        v.setRouteId(routeId);
                        String routeName = staticService.getRouteNameForRoute(routeId);
                        if (routeName != null && !routeName.isBlank()) {
                            v.setRouteName(routeName);
                        } else {
                            // Fallback if the route name is missing but we have an ID
                            v.setRouteName("Route " + routeId);
                        }
                    }

                    v.setLat(vp.hasPosition() ? vp.getPosition().getLatitude() : 0.0);
                    v.setLon(vp.hasPosition() ? vp.getPosition().getLongitude() : 0.0);
                    v.setSpeed(vp.hasPosition() ? vp.getPosition().getSpeed() : 0.0);
                    v.setTimestamp(vp.hasTimestamp() ? vp.getTimestamp() : Instant.now().getEpochSecond());

                    store.save(v);
                }
            }
            log.info("Finished processing {} vehicles.", vehicleCount);
        } catch (Exception e) {
            log.error("Error polling GTFS-RT feed", e);
        }
    }
}
