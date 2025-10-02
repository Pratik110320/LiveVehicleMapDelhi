package com.spring.Live.Vehicle.Map.Delhi.gtfs;

import com.google.transit.realtime.GtfsRealtime;
import com.spring.Live.Vehicle.Map.Delhi.model.RouteDto;
import com.spring.Live.Vehicle.Map.Delhi.model.TripDto;
import com.spring.Live.Vehicle.Map.Delhi.model.VehicleDto;
import com.spring.Live.Vehicle.Map.Delhi.service.GtfsStaticService;
import com.spring.Live.Vehicle.Map.Delhi.service.VehicleStoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

@Component
public class GtfsRtPoller {

    private final WebClient webClient;
    private final VehicleStoreService vehicleStoreService;
    private final GtfsStaticService gtfsStaticService;

    @Value("${gtfs-rt.url}")
    private String gtfsRtUrl;

    @Autowired
    public GtfsRtPoller(WebClient webClient, VehicleStoreService vehicleStoreService, GtfsStaticService gtfsStaticService) {
        this.webClient = webClient;
        this.vehicleStoreService = vehicleStoreService;
        this.gtfsStaticService = gtfsStaticService;
    }

    @Scheduled(fixedRate = 10000) // Poll every 10 seconds
    public void poll() throws IOException {
        GtfsRealtime.FeedMessage feed = GtfsRealtime.FeedMessage.parseFrom(new URL(gtfsRtUrl).openStream());
        for (GtfsRealtime.FeedEntity entity : feed.getEntityList()) {
            if (entity.hasVehicle()) {
                GtfsRealtime.VehiclePosition vehicle = entity.getVehicle();
                VehicleDto vehicleDto = new VehicleDto();
                vehicleDto.setVehicleId(vehicle.getVehicle().getId());
                vehicleDto.setLat(vehicle.getPosition().getLatitude());
                vehicleDto.setLon(vehicle.getPosition().getLongitude());
                vehicleDto.setTripId(vehicle.getTrip().getTripId());

                // Enrich with static GTFS data
                TripDto trip = gtfsStaticService.getTrips().get(vehicle.getTrip().getTripId());
                if (trip != null) {
                    RouteDto route = gtfsStaticService.getRoutes().get(trip.getRouteId());
                    if (route != null) {
                        vehicleDto.setRouteId(route.getRouteId());
                        vehicleDto.setRouteName(route.getRouteLongName() != null && !route.getRouteLongName().isEmpty() ? route.getRouteLongName() : route.getRouteShortName());
                    }
                }

                vehicleStoreService.save(vehicleDto);
            }
        }
    }
}
