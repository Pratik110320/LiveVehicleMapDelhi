package com.spring.Live.Vehicle.Map.Delhi.service;

import com.spring.Live.Vehicle.Map.Delhi.model.*;
import com.spring.Live.Vehicle.Map.Delhi.model.Calendar;
import com.spring.Live.Vehicle.Map.Delhi.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ArrivalPredictionService {

    private static final Logger logger = LoggerFactory.getLogger(ArrivalPredictionService.class);
    private static final ZoneId DELHI_ZONE = ZoneId.of("Asia/Kolkata");

    @Autowired
    private StopTimeRepository stopTimeRepository;

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private RouteRepository routeRepository;

    @Autowired
    private CalendarRepository calendarRepository;

    @Autowired
    private VehicleStoreService vehicleStoreService;

    /**
     * Get real-time arrival predictions for a specific stop
     * This combines scheduled times with live vehicle positions
     */
    public List<ArrivalPrediction> getPredictionsForStop(String stopId, int maxPredictions) {
        logger.info("Generating predictions for stop: {}", stopId);

        // Get all scheduled stop times for this stop
        List<StopTime> scheduledStops = stopTimeRepository.findByStopId(stopId);

        if (scheduledStops.isEmpty()) {
            logger.warn("No scheduled stops found for stopId: {}", stopId);
            return Collections.emptyList();
        }

        // Get active services for today
        Set<String> activeServiceIds = getActiveServicesForToday();

        // Get current time in Delhi timezone
        LocalTime currentTime = LocalTime.now(DELHI_ZONE);
        LocalDateTime now = LocalDateTime.now(DELHI_ZONE);

        // Get all live vehicles
        Map<String, Vehicle> liveVehicles = vehicleStoreService.getAllVehicles();

        // Build predictions
        List<ArrivalPrediction> predictions = new ArrayList<>();

        for (StopTime stopTime : scheduledStops) {
            try {
                // Get the trip for this stop time
                Optional<Trip> tripOpt = tripRepository.findById(stopTime.getTripId());
                if (tripOpt.isEmpty()) continue;

                Trip trip = tripOpt.get();

                // Check if this trip's service is active today
                if (!activeServiceIds.contains(trip.getServiceId())) {
                    continue;
                }

                // Parse scheduled arrival time
                LocalTime scheduledArrival = parseGtfsTime(stopTime.getArrivalTime());
                if (scheduledArrival == null || scheduledArrival.isBefore(currentTime)) {
                    continue; // Skip past arrivals
                }

                // Get route information
                Optional<Route> routeOpt = routeRepository.findById(trip.getRouteId());
                if (routeOpt.isEmpty()) continue;

                Route route = routeOpt.get();

                // Find if there's a live vehicle on this trip
                Vehicle liveVehicle = findVehicleForTrip(trip.getTripId(), liveVehicles);

                ArrivalPrediction prediction = new ArrivalPrediction();
                prediction.setStopId(stopId);
                prediction.setTripId(trip.getTripId());
                prediction.setRouteId(route.getRouteId());
                prediction.setRouteName(route.getRouteShortName());
                prediction.setRouteLongName(route.getRouteLongName());
                prediction.setScheduledArrivalTime(scheduledArrival);

                if (liveVehicle != null) {
                    // Calculate predicted arrival based on vehicle position
                    PredictionResult result = calculatePredictedArrival(
                            liveVehicle,
                            stopTime,
                            trip.getTripId(),
                            scheduledArrival
                    );

                    prediction.setPredictedArrivalTime(result.predictedTime);
                    prediction.setDelayMinutes(result.delayMinutes);
                    prediction.setConfidence(result.confidence);
                    prediction.setVehicleId(liveVehicle.getVehicleId());
                    prediction.setStopsAway(result.stopsAway);
                    prediction.setIsRealTime(true);
                    prediction.setOccupancyStatus(liveVehicle.getOccupancyStatus());
                } else {
                    // No live vehicle - use scheduled time
                    prediction.setPredictedArrivalTime(scheduledArrival);
                    prediction.setDelayMinutes(0);
                    prediction.setConfidence(0.5); // Lower confidence without live data
                    prediction.setIsRealTime(false);
                }

                // Calculate minutes until arrival
                int minutesUntil = (int) Duration.between(
                        LocalDateTime.of(LocalDate.now(), currentTime),
                        LocalDateTime.of(LocalDate.now(), prediction.getPredictedArrivalTime())
                ).toMinutes();

                prediction.setMinutesUntilArrival(minutesUntil);

                predictions.add(prediction);

            } catch (Exception e) {
                logger.error("Error processing stop time: {}", stopTime.getTripId(), e);
            }
        }

        // Sort by predicted arrival time and limit results
        return predictions.stream()
                .sorted(Comparator.comparing(ArrivalPrediction::getPredictedArrivalTime))
                .limit(maxPredictions)
                .collect(Collectors.toList());
    }

    /**
     * Calculate predicted arrival time based on vehicle's current position
     */
    private PredictionResult calculatePredictedArrival(
            Vehicle vehicle,
            StopTime targetStop,
            String tripId,
            LocalTime scheduledArrival) {

        PredictionResult result = new PredictionResult();

        try {
            // Get all stops for this trip in sequence
            List<StopTime> tripStops = stopTimeRepository.findByTripIdOrderByStopSequence(tripId);

            // Find where the vehicle currently is in the sequence
            int currentStopIndex = findCurrentStopIndex(vehicle, tripStops);
            int targetStopIndex = findStopIndex(targetStop.getStopId(), tripStops);

            if (currentStopIndex == -1 || targetStopIndex == -1 || currentStopIndex >= targetStopIndex) {
                // Vehicle hasn't started trip or already passed the stop
                result.predictedTime = scheduledArrival;
                result.delayMinutes = 0;
                result.confidence = 0.3;
                result.stopsAway = 0;
                return result;
            }

            // Calculate stops away
            result.stopsAway = targetStopIndex - currentStopIndex;

            // Get current stop's scheduled time
            StopTime currentStop = tripStops.get(currentStopIndex);
            LocalTime currentScheduled = parseGtfsTime(currentStop.getArrivalTime());

            // Calculate how late/early the vehicle is right now
            LocalTime now = LocalTime.now(DELHI_ZONE);
            long currentDelayMinutes = Duration.between(currentScheduled, now).toMinutes();

            // Estimate travel time to target stop
            long scheduledTravelTime = Duration.between(
                    parseGtfsTime(currentStop.getArrivalTime()),
                    parseGtfsTime(targetStop.getArrivalTime())
            ).toMinutes();

            // Apply delay factor (vehicle tends to maintain current delay)
            // More sophisticated: apply decay factor for delay recovery
            double delayDecayFactor = 0.8; // Vehicle might recover 20% of delay
            long estimatedDelayAtTarget = (long) (currentDelayMinutes * delayDecayFactor);

            // Consider vehicle speed if available
            if (vehicle.getSpeed() > 0) {
                // If vehicle is moving slower than typical, increase delay
                double typicalSpeed = 25.0; // km/h typical for Delhi buses
                if (vehicle.getSpeed() < typicalSpeed * 0.7) {
                    estimatedDelayAtTarget += 2; // Add 2 minutes for slow traffic
                }
            }

            // Calculate final prediction
            LocalTime basePrediction = scheduledArrival.plusMinutes(estimatedDelayAtTarget);

            // Ensure prediction is in the future
            if (basePrediction.isBefore(now)) {
                basePrediction = now.plusMinutes(result.stopsAway * 2); // Estimate 2 min per stop
            }

            result.predictedTime = basePrediction;
            result.delayMinutes = (int) estimatedDelayAtTarget;

            // Calculate confidence based on how far away the vehicle is
            if (result.stopsAway <= 2) {
                result.confidence = 0.95; // Very high confidence when close
            } else if (result.stopsAway <= 5) {
                result.confidence = 0.85;
            } else if (result.stopsAway <= 10) {
                result.confidence = 0.75;
            } else {
                result.confidence = 0.60; // Lower confidence for distant predictions
            }

        } catch (Exception e) {
            logger.error("Error calculating prediction", e);
            result.predictedTime = scheduledArrival;
            result.delayMinutes = 0;
            result.confidence = 0.3;
            result.stopsAway = 0;
        }

        return result;
    }

    /**
     * Find which stop the vehicle is currently near based on its GPS position
     */
    private int findCurrentStopIndex(Vehicle vehicle, List<StopTime> tripStops) {
        // In production, you'd get Stop objects and calculate distance
        // For now, we'll use a simple heuristic based on timestamp

        LocalTime vehicleTime = Instant.ofEpochSecond(vehicle.getTimestamp())
                .atZone(DELHI_ZONE)
                .toLocalTime();

        // Find the stop time closest to the vehicle's current time
        int closestIndex = -1;
        long minDiff = Long.MAX_VALUE;

        for (int i = 0; i < tripStops.size(); i++) {
            LocalTime stopTime = parseGtfsTime(tripStops.get(i).getArrivalTime());
            if (stopTime == null) continue;

            long diff = Math.abs(Duration.between(stopTime, vehicleTime).toMinutes());
            if (diff < minDiff) {
                minDiff = diff;
                closestIndex = i;
            }
        }

        return closestIndex;
    }

    /**
     * Find the index of a specific stop in the trip sequence
     */
    private int findStopIndex(String stopId, List<StopTime> tripStops) {
        for (int i = 0; i < tripStops.size(); i++) {
            if (tripStops.get(i).getStopId().equals(stopId)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Find a live vehicle that's currently running a specific trip
     */
    private Vehicle findVehicleForTrip(String tripId, Map<String, Vehicle> liveVehicles) {
        return liveVehicles.values().stream()
                .filter(v -> tripId.equals(v.getTripId()))
                .findFirst()
                .orElse(null);
    }

    /**
     * Get service IDs that are active today
     */
    private Set<String> getActiveServicesForToday() {
        LocalDate today = LocalDate.now(DELHI_ZONE);
        DayOfWeek dayOfWeek = today.getDayOfWeek();
        String dateStr = today.format(DateTimeFormatter.BASIC_ISO_DATE);

        List<Calendar> calendars = calendarRepository.findActiveServicesForDate(dateStr);

        return calendars.stream()
                .filter(cal -> isServiceActiveOnDay(cal, dayOfWeek))
                .map(Calendar::getServiceId)
                .collect(Collectors.toSet());
    }

    /**
     * Check if a service calendar is active for a specific day of week
     */
    private boolean isServiceActiveOnDay(Calendar cal, DayOfWeek day) {
        return switch (day) {
            case MONDAY -> cal.isMonday();
            case TUESDAY -> cal.isTuesday();
            case WEDNESDAY -> cal.isWednesday();
            case THURSDAY -> cal.isThursday();
            case FRIDAY -> cal.isFriday();
            case SATURDAY -> cal.isSaturday();
            case SUNDAY -> cal.isSunday();
        };
    }

    /**
     * Parse GTFS time format (HH:MM:SS) which can exceed 24 hours
     */
    private LocalTime parseGtfsTime(String timeStr) {
        if (timeStr == null || timeStr.trim().isEmpty()) {
            return null;
        }

        try {
            String[] parts = timeStr.split(":");
            int hours = Integer.parseInt(parts[0]);
            int minutes = Integer.parseInt(parts[1]);
            int seconds = parts.length > 2 ? Integer.parseInt(parts[2]) : 0;

            // Handle times >= 24:00:00 (next day service)
            if (hours >= 24) {
                hours = hours % 24;
            }

            return LocalTime.of(hours, minutes, seconds);
        } catch (Exception e) {
            logger.warn("Failed to parse time: {}", timeStr);
            return null;
        }
    }

    /**
     * Inner class to hold prediction calculation results
     */
    private static class PredictionResult {
        LocalTime predictedTime;
        int delayMinutes;
        double confidence;
        int stopsAway;
    }

    /**
     * Result object for arrival predictions
     */
    public static class ArrivalPrediction {
        private String stopId;
        private String tripId;
        private String routeId;
        private String routeName;
        private String routeLongName;
        private LocalTime scheduledArrivalTime;
        private LocalTime predictedArrivalTime;
        private int delayMinutes;
        private int minutesUntilArrival;
        private double confidence;
        private String vehicleId;
        private int stopsAway;
        private boolean isRealTime;
        private String occupancyStatus;

        // Getters and Setters
        public String getStopId() { return stopId; }
        public void setStopId(String stopId) { this.stopId = stopId; }

        public String getTripId() { return tripId; }
        public void setTripId(String tripId) { this.tripId = tripId; }

        public String getRouteId() { return routeId; }
        public void setRouteId(String routeId) { this.routeId = routeId; }

        public String getRouteName() { return routeName; }
        public void setRouteName(String routeName) { this.routeName = routeName; }

        public String getRouteLongName() { return routeLongName; }
        public void setRouteLongName(String routeLongName) { this.routeLongName = routeLongName; }

        public LocalTime getScheduledArrivalTime() { return scheduledArrivalTime; }
        public void setScheduledArrivalTime(LocalTime scheduledArrivalTime) {
            this.scheduledArrivalTime = scheduledArrivalTime;
        }

        public LocalTime getPredictedArrivalTime() { return predictedArrivalTime; }
        public void setPredictedArrivalTime(LocalTime predictedArrivalTime) {
            this.predictedArrivalTime = predictedArrivalTime;
        }

        public int getDelayMinutes() { return delayMinutes; }
        public void setDelayMinutes(int delayMinutes) { this.delayMinutes = delayMinutes; }

        public int getMinutesUntilArrival() { return minutesUntilArrival; }
        public void setMinutesUntilArrival(int minutesUntilArrival) {
            this.minutesUntilArrival = minutesUntilArrival;
        }

        public double getConfidence() { return confidence; }
        public void setConfidence(double confidence) { this.confidence = confidence; }

        public String getVehicleId() { return vehicleId; }
        public void setVehicleId(String vehicleId) { this.vehicleId = vehicleId; }

        public int getStopsAway() { return stopsAway; }
        public void setStopsAway(int stopsAway) { this.stopsAway = stopsAway; }

        public boolean isRealTime() { return isRealTime; }
        public void setIsRealTime(boolean isRealTime) { this.isRealTime = isRealTime; }

        public String getOccupancyStatus() { return occupancyStatus; }
        public void setOccupancyStatus(String occupancyStatus) {
            this.occupancyStatus = occupancyStatus;
        }
    }
}