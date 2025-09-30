package com.spring.Live.Vehicle.Map.Delhi.gtfs;


import com.google.transit.realtime.GtfsRealtime;
import com.spring.Live.Vehicle.Map.Delhi.model.VehicleDto;
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
    private final WebClient webClient = WebClient.create();
    private final VehicleStoreService store;


    @Value("${otd.realtime.url}")
    private String feedUrl;


    public GtfsRtPoller(VehicleStoreService store) {
        this.store = store;
    }


    @Scheduled(fixedDelayString = "${otd.poll.ms:10000}")
    public void poll() {
        try {
            byte[] data = webClient.get()
                    .uri(feedUrl)
                    .retrieve()
                    .bodyToMono(byte[].class)
                    .block();
            if (data == null) {
                log.warn("Empty feed bytes");
                return;
            }
            GtfsRealtime.FeedMessage feed = GtfsRealtime.FeedMessage.parseFrom(data);
            for (GtfsRealtime.FeedEntity entity : feed.getEntityList()) {
                if (entity.hasVehicle()) {
                    GtfsRealtime.VehiclePosition vp = entity.getVehicle();
                    VehicleDto v = new VehicleDto();
                    String vehicleId = vp.hasVehicle() && vp.getVehicle().hasId() ? vp.getVehicle().getId() : null;
                    v.setVehicleId(vehicleId);
                    v.setTripId(vp.hasTrip() ? vp.getTrip().getTripId() : null);
                    v.setLat(vp.hasPosition() ? vp.getPosition().getLatitude() : 0.0);
                    v.setLon(vp.hasPosition() ? vp.getPosition().getLongitude() : 0.0);
                    v.setSpeed(vp.hasPosition() ? vp.getPosition().getOdometer() : 0.0);
                    v.setTimestamp(vp.hasTimestamp() ? vp.getTimestamp() : Instant.now().getEpochSecond());
                    store.save(v);
                }
            }
        } catch (Exception e) {
            log.error("Error polling GTFS-RT feed", e);
        }
    }
}