package com.spring.Live.Vehicle.Map.Delhi.controller;

import com.spring.Live.Vehicle.Map.Delhi.service.NotificationService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
public class SseController {

    private final NotificationService notificationService;

    public SseController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * Provides a stream of Server-Sent Events.
     * With spring-boot-starter-webflux, Spring Boot automatically handles the subscription
     * and lifecycle of a reactive Flux stream, converting it into an SSE stream.
     * This eliminates the need for manual SseEmitter management.
     * @return A Flux<String> that represents the continuous stream of notifications.
     */
    @GetMapping(path = "/notifications", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamNotifications() {
        return notificationService.getNotificationStream();
    }
}
