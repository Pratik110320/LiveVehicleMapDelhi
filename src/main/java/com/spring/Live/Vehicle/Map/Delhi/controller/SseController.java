package com.spring.Live.Vehicle.Map.Delhi.controller;

import com.spring.Live.Vehicle.Map.Delhi.service.NotificationService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
public class SseController {

    private final NotificationService notificationService;

    public SseController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping(path = "/sse/vehicles", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamVehicleUpdates() {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        notificationService.addEmitter(emitter);
        return emitter;
    }
}
