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
import java.util.Map;
import java.util.stream.Collectors;


@Service
public class VehicleStoreService {

    private final Logger log = LoggerFactory.getLogger(VehicleStoreService.class);
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper mapper = new ObjectMapper();


    private static final String KEY_PREFIX = "vehicle:latest:";


    public VehicleStoreService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }


    public void save(VehicleDto v) {
        try {
            String key = KEY_PREFIX + (v.getVehicleId() == null ? v.getTripId() : v.getVehicleId());
            String json = mapper.writeValueAsString(v);
            redisTemplate.opsForValue().set(key, json);
            redisTemplate.opsForValue().set("vehicle:lastFeedTs", String.valueOf(Instant.now().getEpochSecond()));
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize vehicle", e);
        }
    }


    public List<VehicleDto> getAll() {
        try {
            Map<Object, Object> entries = redisTemplate.opsForValue().multiGet(redisTemplate.keys(KEY_PREFIX + "*"))
                    .stream()
                    .filter(x -> x != null)
                    .collect(Collectors.toMap(k -> k.toString(), v -> v));
            List<VehicleDto> out = new ArrayList<>();
            for (String key : redisTemplate.keys(KEY_PREFIX + "*")) {
                String json = redisTemplate.opsForValue().get((String) key);
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