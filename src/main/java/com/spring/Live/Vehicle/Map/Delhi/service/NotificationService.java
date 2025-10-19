package com.spring.Live.Vehicle.Map.Delhi.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    // Use a map to manage sinks for each session, preventing memory leaks
    private final Map<String, Sinks.Many<String>> sessionSinks = new ConcurrentHashMap<>();

    public Flux<String> getNotificationStream(String sessionId) {
        logger.info("Client connected with session ID: {}", sessionId);
        Sinks.Many<String> sink = sessionSinks.computeIfAbsent(
                sessionId,
                k -> Sinks.many().multicast().onBackpressureBuffer()
        );

        return sink.asFlux()
                .doOnCancel(() -> cleanupSession(sessionId, "cancelled"))
                .doOnTerminate(() -> cleanupSession(sessionId, "terminated"));
    }

    public void sendNotification(String notification) {
        sessionSinks.forEach((sessionId, sink) -> {
            Sinks.EmitResult result = sink.tryEmitNext(notification);
            if (result.isFailure()) {
                logger.warn("Failed to send notification to session {}: {}", sessionId, result);
            }
        });
    }

    private void cleanupSession(String sessionId, String reason) {
        sessionSinks.remove(sessionId);
        logger.info("Cleaned up session {} because connection was {}", sessionId, reason);
    }
}
