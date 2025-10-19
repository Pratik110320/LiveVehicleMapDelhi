package com.spring.Live.Vehicle.Map.Delhi.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    // This set will store the vehicle IDs that a user is subscribed to.
    // In a real-world application, this would be tied to user sessions or stored in a database.
    private final Set<String> subscriptions = Collections.synchronizedSet(new HashSet<>());
    private final Sinks.Many<String> sink = Sinks.many().multicast().onBackpressureBuffer();
    private final Flux<String> flux = sink.asFlux();
    /**
     * Subscribes a user to receive notifications for a specific vehicle.
     * This method adds the vehicleId to a set of subscriptions.
     * @param vehicleId The ID of the vehicle to subscribe to.
     */
    public void subscribe(String vehicleId) {
        if (vehicleId != null && !vehicleId.trim().isEmpty()) {
            subscriptions.add(vehicleId);
            logger.info("User subscribed to notifications for vehicle ID: {}", vehicleId);
        } else {
            logger.warn("Attempted to subscribe with a null or empty vehicleId.");
        }
    }

    /**
     * Checks if there is a subscription for a given vehicle ID.
     * @param vehicleId The vehicle ID to check.
     * @return true if a subscription exists, false otherwise.
     */
    public boolean isSubscribed(String vehicleId) {
        return subscriptions.contains(vehicleId);
    }

    /**
     * Unsubscribes a user from notifications for a specific vehicle.
     * @param vehicleId The ID of the vehicle to unsubscribe from.
     */
    public void unsubscribe(String vehicleId) {
        subscriptions.remove(vehicleId);
        logger.info("User unsubscribed from notifications for vehicle ID: {}", vehicleId);
    }

    public void sendNotification(String message) {
        logger.info("Broadcasting notification: {}", message);
        sink.tryEmitNext(message);
    }

    public Flux<String> getNotificationStream() {
        return this.flux;
    }
}
