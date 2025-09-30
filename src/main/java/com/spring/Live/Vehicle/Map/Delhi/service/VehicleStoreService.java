package com.spring.Live.Vehicle.Map.Delhi.service;



import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.Live.Vehicle.Map.Delhi.model.VehicleDto;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;


@Service
public class VehicleStoreService {

    private final Logger log = LoggerFactory.getLogger(VehicleStoreService.class);
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper mapper = new ObjectMapper();


    private static final String KEY_PREFIX = "vehicle:latest:";
    private static final long VEHICLE_TTL_MINUTES = 5;


    public VehicleStoreService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }


    public void save(VehicleDto v) {
        try {
            String id = v.getVehicleId() != null ? v.getVehicleId() : v.getTripId();
            if (id == null || id.isBlank()) {
                log.warn("Skipping vehicle save due to missing ID.");
                return;
            }

            String key = KEY_PREFIX + id;
            String json = mapper.writeValueAsString(v);
            redisTemplate.opsForValue().set(key, json);
            redisTemplate.expire(key, VEHICLE_TTL_MINUTES, TimeUnit.MINUTES);
            redisTemplate.opsForValue().set("vehicle:lastFeedTs", String.valueOf(Instant.now().getEpochSecond()));
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize vehicle", e);
        }
    }


    public List<VehicleDto> getAll() {
        try {
            List<VehicleDto> out = new ArrayList<>();
            Set<String> keys = redisTemplate.keys(KEY_PREFIX + "*");
            log.info("API request for all vehicles. Found {} keys in Redis.", keys.size());

            for (String key : keys) {
                String json = redisTemplate.opsForValue().get(key);
                if (json == null) continue;
                VehicleDto dto = mapper.readValue(json, VehicleDto.class);
                out.add(dto);
            }
            return out;
        } catch (Exception e) {
            log.error("Error reading vehicles from redis", e);
            return List.of();
        }
    }


    public Long getLastFeedTs() {
        String s = redisTemplate.opsForValue().get("vehicle:lastFeedTs");
        if (s == null) return null;
        try { return Long.parseLong(s); } catch (NumberFormatException ex) { return null; }
    }
}

