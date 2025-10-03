package com.spring.Live.Vehicle.Map.Delhi.gtfs;

import com.google.transit.realtime.GtfsRealtime;
import com.spring.Live.Vehicle.Map.Delhi.model.RouteDto;
import com.spring.Live.Vehicle.Map.Delhi.model.VehicleDto;
import com.spring.Live.Vehicle.Map.Delhi.service.GtfsStaticService;
import com.spring.Live.Vehicle.Map.Delhi.service.NotificationService;
import com.spring.Live.Vehicle.Map.Delhi.service.VehicleStoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class GtfsRtPoller {

    private static final Logger log = LoggerFactory.getLogger(GtfsRtPoller.class);
    private final VehicleStoreService vehicleStoreService;
    private final GtfsStaticService gtfsStaticService;
    private final NotificationService notificationService;
    private final WebClient webClient;

    @Value("${otd.realtime.url}")
    private String gtfsRtUrl;

    @Autowired
    public GtfsRtPoller(VehicleStoreService vehicleStoreService, GtfsStaticService gtfsStaticService, NotificationService notificationService, WebClient.Builder webClientBuilder) {
        this.vehicleStoreService = vehicleStoreService;
        this.gtfsStaticService = gtfsStaticService;
        this.notificationService = notificationService;
        this.webClient = webClientBuilder.build();
    }
    // (Only the changed/added parts shown; paste into your file replacing poll() and processFeed())
    @Scheduled(fixedRateString = "${otd.realtime.poll.ms:15000}")
    public void poll() {
        if (gtfsRtUrl == null || gtfsRtUrl.isBlank()) {
            log.warn("GTFS-RT URL is not configured. Skipping poll.");
            return;
        }

        log.debug("Polling GTFS-RT feed from {}", gtfsRtUrl);

        webClient.get()
                .uri(gtfsRtUrl)
                .accept(MediaType.APPLICATION_OCTET_STREAM)
                .retrieve()
                .bodyToMono(byte[].class)
                .flatMap(this::processFeed)
                .doOnError(error -> {
                    log.error("Error polling or processing GTFS-RT feed: {}", error.getMessage(), error);
                })
                .onErrorResume(e -> Mono.empty())
                .subscribe();
    }

    private Mono<Void> processFeed(byte[] feedBytes) {
        return Mono.fromRunnable(() -> {
            if (feedBytes == null || feedBytes.length == 0) {
                log.warn("GTFS-RT feed bytes empty");
                return;
            }

            try {
                GtfsRealtime.FeedMessage feed = GtfsRealtime.FeedMessage.parseFrom(feedBytes);
                int entityCount = feed.getEntityCount();
                log.debug("Parsed feed with {} entities (bytes={})", entityCount, feedBytes.length);

                List<VehicleDto> updatedVehicles = new ArrayList<>();
                Map<String, RouteDto> routes = gtfsStaticService.getRoutes();

                for (GtfsRealtime.FeedEntity entity : feed.getEntityList()) {
                    try {
                        // Ensure we have any vehicle info
                        if (!entity.hasVehicle()) {
                            log.trace("Entity {} has no vehicle; skipping", entity.getId());
                            continue;
                        }

                        GtfsRealtime.VehiclePosition vehicle = entity.getVehicle();

                        if (!vehicle.hasPosition() || !vehicle.hasTrip()) {
                            log.trace("Vehicle missing position or trip for entity {}. pos={} trip={}",
                                    entity.getId(), vehicle.hasPosition(), vehicle.hasTrip());
                            continue;
                        }

                        // Fallback for vehicle id: try VehicleDescriptor.id then entity.id
                        String vehicleId = null;
                        if (vehicle.hasVehicle() && vehicle.getVehicle().hasId()) {
                            vehicleId = vehicle.getVehicle().getId();
                        }
                        if (vehicleId == null || vehicleId.isBlank()) {
                            // fallback to feed entity id
                            vehicleId = entity.getId();
                        }
                        if (vehicleId == null || vehicleId.isBlank()) {
                            log.warn("Could not determine vehicle id for entity {}; skipping", entity.getId());
                            continue;
                        }

                        double lat = vehicle.getPosition().getLatitude();
                        double lon = vehicle.getPosition().getLongitude();

                        VehicleDto newVehicleDto = new VehicleDto();
                        newVehicleDto.setVehicleId(vehicleId);
                        newVehicleDto.setLat(lat);
                        newVehicleDto.setLon(lon);
                        if (vehicle.getPosition().hasSpeed()) newVehicleDto.setSpeed(vehicle.getPosition().getSpeed());
                        if (vehicle.hasTimestamp()) newVehicleDto.setTimestamp(vehicle.getTimestamp());
                        if (vehicle.getTrip().hasTripId()) newVehicleDto.setTripId(vehicle.getTrip().getTripId());

                        String routeId = vehicle.getTrip().hasRouteId() ? vehicle.getTrip().getRouteId() : null;
                        newVehicleDto.setRouteId(routeId);
                        if (routeId != null) {
                            RouteDto route = routes.get(routeId);
                            if (route != null) {
                                String shortName = route.getRouteShortName();
                                String longName = route.getRouteLongName();
                                newVehicleDto.setRouteName(shortName != null && !shortName.isBlank() ? shortName : longName);
                            } else {
                                newVehicleDto.setRouteName(routeId);
                            }
                        } else {
                            newVehicleDto.setRouteName("unknown-route");
                        }

                        Optional<VehicleDto> existingVehicleOpt = vehicleStoreService.getById(newVehicleDto.getVehicleId());
                        boolean positionHasChanged = existingVehicleOpt.map(existing ->
                                Math.abs(existing.getLat() - newVehicleDto.getLat()) > 0.00001 ||
                                        Math.abs(existing.getLon() - newVehicleDto.getLon()) > 0.00001
                        ).orElse(true);

                        if (positionHasChanged) {
                            vehicleStoreService.save(newVehicleDto);
                            updatedVehicles.add(newVehicleDto);
                            log.trace("Updated vehicle {} at {},{}", vehicleId, lat, lon);
                        }
                    } catch (Exception e) {
                        log.warn("Error processing entity {}: {}", entity.getId(), e.getMessage(), e);
                    }
                }

                int emitterCount = notificationService.getEmitterCount();
                if (!updatedVehicles.isEmpty()) {
                    log.info("Poll found {} updated vehicles. Pushing to {} clients.", updatedVehicles.size(), emitterCount);
                    try {
                        notificationService.sendVehicleUpdate(updatedVehicles);
                    } catch (Exception e) {
                        log.error("Failed to send vehicle update: {}", e.getMessage(), e);
                    }
                } else {
                    log.debug("Poll completed, but no vehicle positions have changed. Emitters={}", emitterCount);
                    try {
                        notificationService.sendHeartbeat();
                    } catch (Exception e) {
                        log.debug("sendHeartbeat failed: {}", e.getMessage(), e);
                    }
                }
            } catch (Exception e) {
                log.error("Failed to parse GTFS-RT feed", e);
            }
        });
    }

}
