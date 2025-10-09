package com.spring.Live.Vehicle.Map.Delhi.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.Live.Vehicle.Map.Delhi.model.Vehicle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
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

    public void sendVehicleUpdate(List<Vehicle> vehicles) {
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
        senAllEmitters(event);
    }

    public void sendHeartbeat() {
        SseEmitter.SseEventBuilder event = SseEmitter.event().name("heartbeat").data("ping");
        senAllEmitters(event);
    }

    private void senAllEmitters(SseEmitter.SseEventBuilder event) {
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

