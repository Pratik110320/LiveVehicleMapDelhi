//package com.spring.Live.Vehicle.Map.Delhi.service;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.spring.Live.Vehicle.Map.Delhi.model.Vehicle;
//import org.slf4j.LoggerFactory;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.stereotype.Service;
//import org.slf4j.Logger;
//
//import java.time.Instant;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Optional;
//import java.util.Set;
//import java.util.concurrent.TimeUnit;
//import java.util.stream.Collectors;
//
//@Service
//public class VehicleStoreService {
//
//    private final Logger log = LoggerFactory.getLogger(VehicleStoreService.class);
//    private final RedisTemplate<String, String> redisTemplate;
//    private final ObjectMapper mapper = new ObjectMapper();
//
//    private static final String KEY_PREFIX = "vehicle:latest:";
//    private static final long VEHICLE_TTL_MINUTES = 5;
//
//    public VehicleStoreService(RedisTemplate<String, String> redisTemplate) {
//        this.redisTemplate = redisTemplate;
//    }
//
//    public void save(Vehicle v) {
//        try {
//            String id = v.getVehicleId();
//            if (id == null || id.isBlank()) {
//                log.warn("Skipping vehicle save due to missing Vehicle ID.");
//                return;
//            }
//
//            String key = KEY_PREFIX + id;
//            String json = mapper.writeValueAsString(v);
//            redisTemplate.opsForValue().set(key, json, VEHICLE_TTL_MINUTES, TimeUnit.MINUTES);
//            redisTemplate.opsForValue().set("vehicle:lastFeedTs", String.valueOf(Instant.now().getEpochSecond()));
//        } catch (JsonProcessingException e) {
//            log.error("Failed to serialize vehicle", e);
//        }
//    }
//
//    public List<Vehicle> getAll() {
//        Set<String> keys = redisTemplate.keys(KEY_PREFIX + "*");
//        if (keys == null || keys.isEmpty()) {
//            return List.of();
//        }
//
//        List<String> jsonValues = redisTemplate.opsForValue().multiGet(keys);
//        if (jsonValues == null) {
//            return List.of();
//        }
//
//        return jsonValues.stream()
//                .filter(json -> json != null && !json.isEmpty())
//                .map(json -> {
//                    try {
//                        return mapper.readValue(json, Vehicle.class);
//                    } catch (JsonProcessingException e) {
//                        log.error("Error reading vehicle from redis", e);
//                        return null;
//                    }
//                })
//                .filter( ->  != null)
//                .collect(Collectors.toList());
//    }
//
//    public Optional<Vehicle> getById(String vehicleId) {
//        try {
//            String key = KEY_PREFIX + vehicleId;
//            String json = redisTemplate.opsForValue().get(key);
//            if (json == null) {
//                return Optional.empty();
//            }
//            return Optional.of(mapper.readValue(json, Vehicle.class));
//        } catch (Exception e) {
//            log.error("Error reading vehicle {} from redis", vehicleId, e);
//            return Optional.empty();
//        }
//    }
//
//    public Long getLastFeedTs() {
//        String s = redisTemplate.opsForValue().get("vehicle:lastFeedTs");
//        if (s == null) return null;
//        try { return Long.parseLong(s); } catch (NumberFormatException ex) { return null; }
//    }
//}
