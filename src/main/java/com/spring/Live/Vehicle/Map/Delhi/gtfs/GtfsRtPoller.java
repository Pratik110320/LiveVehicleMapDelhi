//package com.spring.Live.Vehicle.Map.Delhi.gtfs;
//
//import com.google.transit.realtime.GtfsRealtime;
//import com.spring.Live.Vehicle.Map.Delhi.model.Route;
//import com.spring.Live.Vehicle.Map.Delhi.model.Trip;
//import com.spring.Live.Vehicle.Map.Delhi.model.Vehicle;
//import com.spring.Live.Vehicle.Map.Delhi.service.GtfsStaticService;
//import com.spring.Live.Vehicle.Map.Delhi.service.NotificationService;
//import com.spring.Live.Vehicle.Map.Delhi.service.VehicleStoreService;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.MediaType;
//import org.springframework.scheduling.annotation.EnableScheduling;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//import org.springframework.web.reactive.function.client.WebClient;
//import reactor.core.publisher.Mono;
//
//import java.util.List;
//import java.util.Objects;
//import java.util.Set;
//import java.util.stream.Collectors;
//
//@Component
//@EnableScheduling
//public class GtfsRtPoller {
//
//    private static final Logger log = LoggerFactory.getLogger(GtfsRtPoller.class);
//    private final VehicleStoreService vehicleStoreService;
//    private final GtfsStaticService gtfsStaticService;
//    private final NotificationService notificationService;
//    private final WebClient webClient;
//
//    @Value("${otd.realtime.url}")
//    private String gtfsRtUrl;
//
//    @Autowired
//    public GtfsRtPoller(VehicleStoreService vehicleStoreService,
//                        GtfsStaticService gtfsStaticService,
//                        NotificationService notificationService,
//                        WebClient webClient) {
//        this.vehicleStoreService = vehicleStoreService;
//        this.gtfsStaticService = gtfsStaticService;
//        this.notificationService = notificationService;
//        this.webClient = webClient;
//    }
//
//    @Scheduled(fixedRateString = "${otd.realtime.poll.ms:15000}")
//    public void poll() {
//        if (gtfsRtUrl == null || gtfsRtUrl.isBlank()) {
//            log.warn("GTFS-RT URL is not configured. Skipping poll.");
//            return;
//        }
//
//        log.info("Polling GTFS-RT feed from {}", gtfsRtUrl);
//
//        webClient.get()
//                .uri(gtfsRtUrl)
//                .accept(MediaType.APPLICATION_OCTET_STREAM)
//                .retrieve()
//                .bodyToMono(byte[].class)
//                .flatMap(this::processFeed)
//                .doOnError(error -> log.error("Error polling or processing GTFS-RT feed: {}", error.getMessage(), error))
//                .onErrorResume(e -> Mono.empty()) // Don't stop scheduling on error
//                .subscribe();
//    }
//
//    private Mono<Void> processFeed(byte[] feedBytes) {
//        return Mono.fromRunnable(() -> {
//            if (feedBytes == null || feedBytes.length == 0) {
//                log.warn("GTFS-RT feed bytes empty");
//                return;
//            }
//
//            try {
//                GtfsRealtime.FeedMessage feed = GtfsRealtime.FeedMessage.parseFrom(feedBytes);
//                // Get the set of currently active service IDs from the static service
//                Set<String> activeServiceIds = gtfsStaticService.getActiveServiceIds();
//                if (activeServiceIds.isEmpty()) {
//                    log.warn("No active service IDs found for today. No vehicles will be processed.");
//                } else {
//                    log.info("Found {} active service IDs for today. Filtering vehicles.", activeServiceIds.size());
//                }
//
//                List<Vehicle> updatedVehicles = feed.getEntityList().stream()
//                        .filter(entity -> entity.hasVehicle() && entity.getVehicle().hasTrip())
//                        .map(this::createVehicle) // First, create the
//                        .filter(Objects::nonNull)
//                        // CRITICAL FIX: Only include vehicles whose trip is running on an active service today
//                        .filter( -> {
//                            Trip trip = gtfsStaticService.getTripById(.getTripId());
//                            return trip != null && activeServiceIds.contains(trip.getServiceId());
//                        })
//                        .collect(Collectors.toList());
//
//                if (!updatedVehicles.isEmpty()) {
//                    updatedVehicles.forEach(vehicleStoreService::save);
//                    notificationService.sendVehicleUpdate(updatedVehicles);
//                    log.info("Processed and sent updates for {} active vehicles.", updatedVehicles.size());
//                } else {
//                    log.info("No active vehicle updates in this poll.");
//                    notificationService.sendHeartbeat(); // Send heartbeat to keep clients connected
//                }
//
//            } catch (Exception e) {
//                log.error("Failed to parse GTFS-RT feed", e);
//            }
//        });
//    }
//
//
//    private Vehicle createVehicle(GtfsRealtime.FeedEntity entity) {
//        GtfsRealtime.VehiclePosition vehicle = entity.getVehicle();
//        if (!vehicle.hasPosition() || !vehicle.hasTrip() || !vehicle.hasVehicle()) return null;
//
//        String vehicleId = vehicle.getVehicle().getId();
//        if (vehicleId == null || vehicleId.isBlank()) return null;
//
//        Vehicle  = new Vehicle();
//        .setVehicleId(vehicleId);
//        .setLat(vehicle.getPosition().getLatitude());
//        .setLon(vehicle.getPosition().getLongitude());
//        if (vehicle.getPosition().hasSpeed()) .setSpeed(vehicle.getPosition().getSpeed());
//        if (vehicle.hasTimestamp()) .setTimestamp(vehicle.getTimestamp());
//
//        GtfsRealtime.TripDescriptor trip = vehicle.getTrip();
//        .setTripId(trip.getTripId());
//
//        // Enrich with static data
//        Trip trip = gtfsStaticService.getTripById(trip.getTripId());
//        if (trip != null) {
//            .setTripHeadsign(trip.getTripHeadsign());
//            Route route = gtfsStaticService.getRouteById(trip.getRouteId());
//            if (route != null) {
//                .setRouteId(route.getRouteId());
//                .setRouteName(route.getRouteShortName());
//            }
//        }
//        return ;
//    }
//}
//
