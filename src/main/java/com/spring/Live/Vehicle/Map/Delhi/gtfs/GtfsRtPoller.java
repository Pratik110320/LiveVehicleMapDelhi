package com.spring.Live.Vehicle.Map.Delhi.gtfs;

import com.google.transit.realtime.GtfsRealtime;
import com.spring.Live.Vehicle.Map.Delhi.model.RouteDto;
import com.spring.Live.Vehicle.Map.Delhi.model.TripDto;
import com.spring.Live.Vehicle.Map.Delhi.model.VehicleDto;
import com.spring.Live.Vehicle.Map.Delhi.service.GtfsStaticService;
import com.spring.Live.Vehicle.Map.Delhi.service.NotificationService;
import com.spring.Live.Vehicle.Map.Delhi.service.VehicleStoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@EnableScheduling
public class GtfsRtPoller {

    private static final Logger log = LoggerFactory.getLogger(GtfsRtPoller.class);
    private final VehicleStoreService vehicleStoreService;
    private final GtfsStaticService gtfsStaticService;
    private final NotificationService notificationService;
    private final WebClient webClient;

    @Value("${otd.realtime.url}")
    private String gtfsRtUrl;

    @Autowired
    public GtfsRtPoller(VehicleStoreService vehicleStoreService,
                        GtfsStaticService gtfsStaticService,
                        NotificationService notificationService,
                        WebClient webClient) { // Inject the configured WebClient bean
        this.vehicleStoreService = vehicleStoreService;
        this.gtfsStaticService = gtfsStaticService;
        this.notificationService = notificationService;
        this.webClient = webClient;
    }

    @Scheduled(fixedRateString = "${otd.realtime.poll.ms:15000}")
    public void poll() {
        if (gtfsRtUrl == null || gtfsRtUrl.isBlank()) {
            log.warn("GTFS-RT URL is not configured. Skipping poll.");
            return;
        }

        log.info("Polling GTFS-RT feed from {}", gtfsRtUrl);

        webClient.get()
                .uri(gtfsRtUrl)
                .accept(MediaType.APPLICATION_OCTET_STREAM)
                .retrieve()
                .bodyToMono(byte[].class)
                .flatMap(this::processFeed)
                .doOnError(error -> log.error("Error polling or processing GTfs-RT feed: {}", error.getMessage(), error))
                .onErrorResume(e -> Mono.empty()) // Don't stop scheduling on error
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
                log.info("Parsed feed with {} entities.", feed.getEntityCount());

                List<VehicleDto> updatedVehicles = feed.getEntityList().stream()
                        .map(this::createVehicleDto)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());

                // Now that we have all vehicles, update the store and notify
                if (!updatedVehicles.isEmpty()) {
                    updatedVehicles.forEach(vehicleStoreService::save);
                    notificationService.sendVehicleUpdate(updatedVehicles);
                    log.info("Processed and sent updates for {} vehicles.", updatedVehicles.size());
                } else {
                    log.info("No vehicle updates in this poll.");
                    notificationService.sendHeartbeat();
                }

            } catch (Exception e) {
                log.error("Failed to parse GTFS-RT feed", e);
            }
        });
    }


    private VehicleDto createVehicleDto(GtfsRealtime.FeedEntity entity) {
        if (!entity.hasVehicle()) return null;

        GtfsRealtime.VehiclePosition vehicle = entity.getVehicle();
        if (!vehicle.hasPosition() || !vehicle.hasTrip() || !vehicle.hasVehicle()) return null;

        String vehicleId = vehicle.getVehicle().getId();
        if (vehicleId == null || vehicleId.isBlank()) return null; // We need an ID to track it

        VehicleDto dto = new VehicleDto();
        dto.setVehicleId(vehicleId);
        dto.setLat(vehicle.getPosition().getLatitude());
        dto.setLon(vehicle.getPosition().getLongitude());
        if (vehicle.getPosition().hasSpeed()) dto.setSpeed(vehicle.getPosition().getSpeed());
        if (vehicle.hasTimestamp()) dto.setTimestamp(vehicle.getTimestamp());

        GtfsRealtime.TripDescriptor trip = vehicle.getTrip();
        dto.setTripId(trip.getTripId());

        // Enrich with static data
        TripDto tripDto = gtfsStaticService.getTripById(trip.getTripId());
        if (tripDto != null) {
            dto.setRouteId(tripDto.getRouteId());
            RouteDto routeDto = gtfsStaticService.getRoutes().get(tripDto.getRouteId());
            if (routeDto != null) {
                dto.setRouteName(routeDto.getRouteShortName());
            }
        }
        return dto;
    }
}
