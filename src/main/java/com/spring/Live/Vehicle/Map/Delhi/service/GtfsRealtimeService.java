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
import java.util.stream.Collectors;

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

    public List<VehiclePosition> fetchVehiclePositions(Double minLat, Double maxLat, Double minLon, Double maxLon, Integer limit) {
        try {
            String urlWithKey = feedUrl;
            if (apiKey != null && !apiKey.trim().isEmpty() && !apiKey.equals("YOUR_PRIVATE_KEY_HERE")) {
                if (urlWithKey.contains("?")) {
                    urlWithKey += "&key=" + apiKey;
                } else {
                    urlWithKey += "?key=" + apiKey;
                }
            }
            log.debug("Fetching GTFS-RT feed from: {}", urlWithKey);

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
                if (!vp.hasPosition()) continue;

                VehiclePosition dto = new VehiclePosition();

                String vid = null;
                if (vp.hasVehicle() && vp.getVehicle().hasId()) vid = vp.getVehicle().getId();
                else if (entity.hasId()) vid = entity.getId();
                dto.setVehicleId(vid);

                String route = vp.hasTrip() && vp.getTrip().hasRouteId()
                        ? vp.getTrip().getRouteId() : null;
                dto.setRouteId(route);

                dto.setLatitude(vp.getPosition().getLatitude());
                dto.setLongitude(vp.getPosition().getLongitude());
                dto.setBearing(vp.getPosition().hasBearing() ? vp.getPosition().getBearing() : null);

                // --- PERFORMANCE OPTIMIZATION ---
                // Convert m/s to km/h here on the backend
                if (vp.getPosition().hasSpeed()) {
                    dto.setSpeed(vp.getPosition().getSpeed() * 3.6f); // m/s to km/h
                } else {
                    dto.setSpeed(null);
                }

                dto.setTimestamp(vp.hasTimestamp() ? vp.getTimestamp() : null);

                positions.add(dto);
            }
            log.info("Parsed {} total vehicle positions", positions.size());

            // --- PERFORMANCE OPTIMIZATION ---
            // Filter the list *on the backend* before sending it to the client.
            // This massively reduces payload size and client-side processing.
            List<VehiclePosition> filteredPositions = positions;

            if (minLat != null && maxLat != null && minLon != null && maxLon != null) {
                filteredPositions = positions.stream()
                        .filter(vp -> vp.getLatitude() >= minLat && vp.getLatitude() <= maxLat &&
                                vp.getLongitude() >= minLon && vp.getLongitude() <= maxLon)
                        .collect(Collectors.toList());
                log.info("Filtered to {} positions within map bounds", filteredPositions.size());
            }

            // Apply the limit *after* filtering
            if (limit != null && filteredPositions.size() > limit) {
                log.info("Sampling {} vehicles down to limit of {}", filteredPositions.size(), limit);
                // Simple limit, not random sampling
                return filteredPositions.subList(0, limit);
            }

            return filteredPositions;

        } catch (Exception ex) {
            log.error("Error fetching/parsing GTFS-RT feed", ex);
            return new ArrayList<>();
        }
    }
}
