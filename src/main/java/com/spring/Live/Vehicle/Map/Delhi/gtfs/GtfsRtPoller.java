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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Component
public class GtfsRtPoller {

    private static final Logger log = LoggerFactory.getLogger(GtfsRtPoller.class);
    private final VehicleStoreService vehicleStoreService;
    private final GtfsStaticService gtfsStaticService;
    private final NotificationService notificationService;

    @Value("${otd.realtime.url}")
    private String gtfsRtUrl;

    @Autowired
    public GtfsRtPoller(VehicleStoreService vehicleStoreService, GtfsStaticService gtfsStaticService, NotificationService notificationService) {
        this.vehicleStoreService = vehicleStoreService;
        this.gtfsStaticService = gtfsStaticService;
        this.notificationService = notificationService;
    }

    @Scheduled(fixedRateString = "${otd.realtime.poll.ms:15000}")
    public void poll() {
        log.debug("Polling GTFS-RT feed from {}", gtfsRtUrl);
        try {
            GtfsRealtime.FeedMessage feed = GtfsRealtime.FeedMessage.parseFrom(new URL(gtfsRtUrl).openStream());
            List<VehicleDto> updatedVehicles = new ArrayList<>();

            for (GtfsRealtime.FeedEntity entity : feed.getEntityList()) {
                if (entity.hasVehicle()) {
                    GtfsRealtime.VehiclePosition vehicle = entity.getVehicle();
                    VehicleDto vehicleDto = new VehicleDto();
                    vehicleDto.setVehicleId(vehicle.getVehicle().getId());

                    if (vehicle.hasPosition()) {
                        vehicleDto.setLat(vehicle.getPosition().getLatitude());
                        vehicleDto.setLon(vehicle.getPosition().getLongitude());
                        vehicleDto.setSpeed(vehicle.getPosition().getSpeed());
                    }

                    vehicleDto.setTimestamp(vehicle.getTimestamp());

                    if (vehicle.hasTrip()) {
                        vehicleDto.setTripId(vehicle.getTrip().getTripId());
                        TripDto trip = gtfsStaticService.getTrips().get(vehicle.getTrip().getTripId());
                        if (trip != null) {
                            RouteDto route = gtfsStaticService.getRoutes().get(trip.getRouteId());
                            if (route != null) {
                                vehicleDto.setRouteId(route.getRouteId());
                                String shortName = route.getRouteShortName();
                                String longName = route.getRouteLongName();
                                vehicleDto.setRouteName(shortName != null && !shortName.isBlank() ? shortName : longName);
                            }
                        }
                    }

                    vehicleStoreService.save(vehicleDto);
                    updatedVehicles.add(vehicleDto);
                }
            }

            if (!updatedVehicles.isEmpty()) {
                log.info("Successfully polled and updated {} vehicles. Pushing to {} clients.", updatedVehicles.size(), notificationService.getEmitterCount());
                notificationService.sendVehicleUpdate(updatedVehicles);
            }

        } catch (Exception e) {
            log.error("Error polling GTFS-RT feed", e);
        }
    }
}
