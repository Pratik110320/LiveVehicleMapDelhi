package com.spring.Live.Vehicle.Map.Delhi.controller;

import com.spring.Live.Vehicle.Map.Delhi.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Controller dedicated to handling Server-Sent Events (SSE) for real-time
 * vehicle position updates.
 */
@Controller
public class SseController {

    private static final Logger log = LoggerFactory.getLogger(SseController.class);
    private final NotificationService notificationService;

    @Autowired
    public SseController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * Establishes an SSE connection with a client.
     * The client will receive live vehicle data through this stream.
     *
     * @return SseEmitter object for the client connection.
     */
    @GetMapping("/vehicles")
    public SseEmitter streamVehiclePositions() {
        SseEmitter emitter = new SseEmitter(300_000L); // 5 minute timeout
        notificationService.addEmitter(emitter);

        emitter.onCompletion(() -> {
            log.info("Emitter completed: {}", emitter);
            notificationService.removeEmitter(emitter);
        });

        emitter.onTimeout(() -> {
            log.warn("Emitter timed out: {}", emitter);
            emitter.complete();
            notificationService.removeEmitter(emitter);
        });

        emitter.onError(e -> {
            log.error("Emitter error: {}", emitter, e);
            notificationService.removeEmitter(emitter);
        });

        log.info("New SSE emitter connected: {}", emitter);
        return emitter;
    }
}
