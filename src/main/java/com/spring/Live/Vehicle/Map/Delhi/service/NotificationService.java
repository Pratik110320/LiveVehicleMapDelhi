package com.spring.Live.Vehicle.Map.Delhi.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.Live.Vehicle.Map.Delhi.model.Vehicle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void addEmitter(SseEmitter emitter) {
        emitters.add(emitter);
        log.info("Added SSE emitter. total={}", emitters.size());
    }

    public void removeEmitter(SseEmitter emitter) {
        boolean removed = emitters.remove(emitter);
        if (removed) {
            log.info("Removed SSE emitter. total={}", emitters.size());
        }
    }

    /**
     * Public entry point: accept a List of vehicles.
     */
    public void sendVehicleUpdates(List<Vehicle> vehicles) {
        if (vehicles == null) {
            log.debug("sendVehicleUpdates called with null list; nothing to send.");
            return;
        }
        sendVehicleUpdateInternal(vehicles);
    }

    /**
     * Overload: accept a Map of vehicles (most stores use Map<id, Vehicle>).
     * Converts to a List and delegates to the List-based method.
     */
    public void sendVehicleUpdates(Map<String, Vehicle> vehicleMap) {
        if (vehicleMap == null) {
            log.debug("sendVehicleUpdates called with null map; nothing to send.");
            return;
        }
        Collection<Vehicle> values = vehicleMap.values();
        List<Vehicle> vehicles = new ArrayList<>(values);
        sendVehicleUpdateInternal(vehicles);
    }

    /**
     * Single internal method that serializes and sends SSE event to all emitters.
     */
    private void sendVehicleUpdateInternal(List<Vehicle> vehicles) {
        String jsonData;
        try {
            Map<String, Object> eventData = Map.of("type", "vehicles", "payload", vehicles);
            jsonData = objectMapper.writeValueAsString(eventData);
        } catch (JsonProcessingException e) {
            log.error("Error serializing vehicle data for SSE", e);
            return;
        }

        // Send as unnamed event so browser `EventSource.onmessage` receives it
        SseEmitter.SseEventBuilder event = SseEmitter.event().data(jsonData);
        sendAllEmitters(event);
    }

    public void sendHeartbeat() {
        SseEmitter.SseEventBuilder event = SseEmitter.event().name("heartbeat").data("ping");
        sendAllEmitters(event);
    }

    private void sendAllEmitters(SseEmitter.SseEventBuilder event) {
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(event);
            } catch (IOException e) {
                log.warn("Error sending to emitter (will remove). msg={}", e.getMessage());
                removeEmitter(emitter);
            } catch (IllegalStateException ise) {
                // emitter already completed / closed
                log.debug("Emitter in illegal state (removing). msg={}", ise.getMessage());
                removeEmitter(emitter);
            } catch (Exception ex) {
                log.error("Unexpected error sending SSE event: {}", ex.getMessage(), ex);
                removeEmitter(emitter);
            }
        }
    }

    public int getEmitterCount() {
        return emitters.size();
    }
}
