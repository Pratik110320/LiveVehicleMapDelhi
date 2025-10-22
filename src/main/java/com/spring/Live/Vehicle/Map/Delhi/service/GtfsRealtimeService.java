package com.spring.Live.Vehicle.Map.Delhi.service;



import com.google.transit.realtime.GtfsRealtime;
import com.spring.Live.Vehicle.Map.Delhi.model.VehiclePosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class GtfsRealtimeService {
    private static final Logger log = LoggerFactory.getLogger(GtfsRealtimeService.class);

    private final RestTemplate restTemplate;
    private final String feedUrl;
    private final String apiKey;

    public GtfsRealtimeService(RestTemplate restTemplate,
                               @Value("${gtfsrt.feed.url}") String feedUrl,
                               @Value("${gtfsrt.api.key}") String apiKey) {
        this.restTemplate = restTemplate;
        this.feedUrl = feedUrl;
        this.apiKey = apiKey;
    }

    /**
     * Fetches the GTFS-RT protobuf bytes and parses vehicle positions into DTOs.
     * Returns empty list on errors (logs the error).
     */
    public List<VehiclePosition> fetchVehiclePositions() {
        try {
            String urlWithKey = feedUrl;
            if (apiKey != null && !apiKey.trim().isEmpty() && !apiKey.equals("YOUR_PRIVATE_KEY_HERE")) {
                // Append key as query param (the user supplied sample uses ?key=)
                if (urlWithKey.contains("?")) {
                    urlWithKey += "&key=" + apiKey;
                } else {
                    urlWithKey += "?key=" + apiKey;
                }
            }
            log.debug("Fetching GTFS-RT feed from: {}", urlWithKey);

            // get raw bytes
            byte[] feedBytes = restTemplate.getForObject(urlWithKey, byte[].class);
            if (feedBytes == null || feedBytes.length == 0) {
                log.warn("GTFS-RT feed returned no data");
                return new ArrayList<>();
            }

            ByteArrayInputStream bais = new ByteArrayInputStream(feedBytes);
            GtfsRealtime.FeedMessage feed = GtfsRealtime.FeedMessage.parseFrom(bais);

            List<VehiclePosition> positions = new ArrayList<>();
            for (GtfsRealtime.FeedEntity entity : feed.getEntityList()) {
                if (!entity.hasVehicle()) continue;

                GtfsRealtime.VehiclePosition vp = entity.getVehicle();
                if (!vp.hasPosition()) continue; // skip only those with no coordinates

                VehiclePosition dto = new VehiclePosition();

                // Vehicle ID
                String vid = null;
                if (vp.hasVehicle() && vp.getVehicle().hasId()) vid = vp.getVehicle().getId();
                else if (entity.hasId()) vid = entity.getId();
                dto.setVehicleId(vid);

                // Route ID
                String route = vp.hasTrip() && vp.getTrip().hasRouteId()
                        ? vp.getTrip().getRouteId() : null;
                dto.setRouteId(route);

                // Position
                dto.setLatitude(vp.getPosition().getLatitude());
                dto.setLongitude(vp.getPosition().getLongitude());
                dto.setBearing(vp.getPosition().hasBearing() ? vp.getPosition().getBearing() : null);
                dto.setSpeed(vp.getPosition().hasSpeed() ? vp.getPosition().getSpeed() : null);
                dto.setTimestamp(vp.hasTimestamp() ? vp.getTimestamp() : null);

                positions.add(dto);
            }
            log.info("Returning {} vehicle positions", positions.size());


            int max = 500;
            if (positions.size() > max) {
                List<VehiclePosition> sampled = new ArrayList<>(max);
                double step = (double) positions.size() / max;
                for (int i = 0; i < max; i++) {
                    int idx = (int) (i * step);
                    sampled.add(positions.get(idx));
                }
                log.info("Sampled {} out of {} vehicles for UI", sampled.size(), positions.size());
                return sampled;
            }
            return positions;

        } catch (Exception ex) {
            log.error("Error fetching/parsing GTFS-RT feed", ex);
            return new ArrayList<>();
        }
    }
}
