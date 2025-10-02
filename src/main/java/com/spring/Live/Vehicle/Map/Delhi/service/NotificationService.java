package com.spring.Live.Vehicle.Map.Delhi.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.Live.Vehicle.Map.Delhi.model.VehicleDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Service to manage and broadcast messages to connected SSE clients.
 */
@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void addEmitter(SseEmitter emitter) {
        emitters.add(emitter);
    }

    public void removeEmitter(SseEmitter emitter) {
        emitters.remove(emitter);
    }

    /**
     * Sends the list of vehicle data to all connected SSE clients.
     *
     * @param vehicles The list of vehicle DTOs to send.
     */
    public void sendVehicleUpdate(List<VehicleDto> vehicles) {
        String jsonData;
        try {
            jsonData = objectMapper.writeValueAsString(vehicles);
        } catch (JsonProcessingException e) {
            log.error("Error serializing vehicle data for SSE", e);
            return;
        }

        SseEmitter.SseEventBuilder event = SseEmitter.event()
                .data(jsonData)
                .name("message");

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(event);
            } catch (IOException e) {
                log.warn("Error sending to emitter, removing it: {}", e.getMessage());
                removeEmitter(emitter);
            }
        }
    }

    /**
     * Returns the current number of active SSE emitters.
     * @return The count of connected clients.
     */
    public int getEmitterCount() {
        return emitters.size();
    }
}

