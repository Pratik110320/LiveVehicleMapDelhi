package com.spring.Live.Vehicle.Map.Delhi.controller;

import com.spring.Live.Vehicle.Map.Delhi.service.NotificationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
public class SseController {

    @Autowired
    private NotificationService notificationService;

    // Corrected path to match the frontend EventSource URL
    @GetMapping(path = "/api/sse/vehicles", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamNotifications(HttpServletRequest request) {
        // Use HTTP session ID to manage individual client streams
        String sessionId = request.getSession().getId();
        return notificationService.getNotificationStream(sessionId);
    }
}
