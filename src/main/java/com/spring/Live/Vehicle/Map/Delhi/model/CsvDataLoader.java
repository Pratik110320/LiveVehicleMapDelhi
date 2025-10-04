package com.spring.Live.Vehicle.Map.Delhi.model;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.fasterxml.jackson.core.io.NumberInput.parseBigDecimal;

@Component
public class CsvDataLoader {

    private static final Logger logger = LoggerFactory.getLogger(CsvDataLoader.class);

    // Core data storage - only store what's absolutely necessary
    private final Map<String, RouteDto> routesById = new ConcurrentHashMap<>();
    private final Map<String, TripDto> tripsById = new ConcurrentHashMap<>();
    private final Map<String, StopDto> stopsById = new ConcurrentHashMap<>();
    private final Map<String, FareAttributeDto> faresById = new ConcurrentHashMap<>();
    private final List<FareRuleDto> fareRules = Collections.synchronizedList(new ArrayList<>());

    // For stop times - store only references to save memory
    private final Map<String, List<String>> tripToStopSequence = new ConcurrentHashMap<>();
    private final Map<String, List<String>> stopToTripReferences = new ConcurrentHashMap<>();

    // Minimal stop time info storage
    private final Map<String, Map<String, StopTimeDto>> tripStopTimeMap = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        try {
            loadDataWithStreaming();
            logger.info("CSV data loading completed successfully");
            printStatistics();
        } catch (Exception e) {
            logger.error("CsvDataLoader initialization failed", e);
            clearMemory();
        }
    }

    private void loadDataWithStreaming() throws Exception {
        // Load in dependency order using true streaming
        loadRoutesStreaming();
        loadStopsStreaming();
        loadTripsStreaming();
        loadStopTimesStreaming(); // This is usually the largest file

        // Optional files
        loadFareAttributesStreaming();
        loadFareRulesStreaming();
        linkFareRules();
    }

    private void loadRoutesStreaming() throws Exception {
        logger.info("Streaming routes...");
        streamCsvFile("static/routes.txt", (headers, row) -> {
            if (row.length > 0 && row[0] != null && !row[0].isEmpty()) {
                RouteDto route = new RouteDto();
                route.setRouteId(row[0]);
                if (row.length > 1) route.setRouteShortName(row[1]);
                if (row.length > 2) route.setRouteLongName(row[2]);
                // Add other fields as needed

                routesById.put(route.getRouteId(), route);
            }
        });
    }

    private void loadStopsStreaming() throws Exception {
        logger.info("Streaming stops...");
        streamCsvFile("static/stops.txt", (headers, row) -> {
            if (row.length > 0 && row[0] != null && !row[0].isEmpty()) {
                StopDto stop = new StopDto();
                stop.setStopId(row[0]);
                if (row.length > 1) stop.setStopName(row[1]);
                if (row.length > 2) stop.setStopLat(parseDouble(row[2]));
                if (row.length > 3) stop.setStopLon(parseDouble(row[3]));
                // Add other fields as needed

                stopsById.put(stop.getStopId(), stop);
            }
        });
    }

    private void loadTripsStreaming() throws Exception {
        logger.info("Streaming trips...");
        streamCsvFile("static/trips.txt", (headers, row) -> {
            if (row.length > 0 && row[0] != null && !row[0].isEmpty()) {
                TripDto trip = new TripDto();
                trip.setTripId(row[0]);
                if (row.length > 1) trip.setRouteId(row[1]);
                // Add other fields as needed

                tripsById.put(trip.getTripId(), trip);

                // Link to route
                if (trip.getRouteId() != null) {
                    RouteDto route = routesById.get(trip.getRouteId());
                    if (route != null) {
                        route.addTrip(trip);
                    }
                }
            }
        });
    }

    private void loadStopTimesStreaming() throws Exception {
        logger.info("Streaming stop times (memory efficient)...");
        final int[] counter = {0};

        streamCsvFile("static/stop_times.txt", (headers, row) -> {
            if (row.length >= 3 && row[0] != null && row[1] != null && row[2] != null) {
                String tripId = row[0];
                String stopId = row[2];

                // Store minimal relationship info
                tripToStopSequence
                        .computeIfAbsent(tripId, k -> new ArrayList<>())
                        .add(stopId);

                stopToTripReferences
                        .computeIfAbsent(stopId, k -> new ArrayList<>())
                        .add(tripId);

                // Store minimal stop time info if needed
                StopTimeDto stopTime = new StopTimeDto();
                stopTime.setTripId(tripId);
                stopTime.setStopId(stopId);
                if (row.length > 1) stopTime.setArrivalTime(row[1]);
                if (row.length > 3) stopTime.setStopSequence(parseInt(row[3]));

                tripStopTimeMap
                        .computeIfAbsent(tripId, k -> new ConcurrentHashMap<>())
                        .put(stopId, stopTime);

                counter[0]++;
                if (counter[0] % 100000 == 0) {
                    logger.info("Processed {} stop times...", counter[0]);
                    System.gc(); // Suggest GC more frequently for large files
                }
            }
        });

        logger.info("Completed processing {} stop times", counter[0]);
    }

    private void loadFareAttributesStreaming() {
        try {
            streamCsvFile("static/fare_attributes.txt", (headers, row) -> {
                if (row.length > 0 && row[0] != null && !row[0].isEmpty()) {
                    FareAttributeDto fare = new FareAttributeDto();
                    fare.setFareId(row[0]);
                    if (row.length > 1) fare.setPrice(parseBigDecimal(row[1]));                    // Add other fields as needed

                    faresById.put(fare.getFareId(), fare);
                }
            });
        } catch (Exception e) {
            logger.info("Fare attributes not available: {}", e.getMessage());
        }
    }

    private void loadFareRulesStreaming() {
        try {
            streamCsvFile("static/fare_rules.txt", (headers, row) -> {
                if (row.length > 0) {
                    FareRuleDto fareRule = new FareRuleDto();
                    if (row.length > 0) fareRule.setFareId(row[0]);
                    if (row.length > 1) fareRule.setRouteId(row[1]);
                    if (row.length > 2) fareRule.setOriginId(row[2]);
                    if (row.length > 3) fareRule.setDestinationId(row[3]);

                    fareRules.add(fareRule);
                }
            });
        } catch (Exception e) {
            logger.info("Fare rules not available: {}", e.getMessage());
        }
    }

    // True streaming CSV reader - processes one line at a time
    private void streamCsvFile(String resourcePath, CsvRowProcessor processor) throws Exception {
        ClassPathResource resource = new ClassPathResource(resourcePath);
        if (!resource.exists()) {
            logger.warn("CSV file not found: {}", resourcePath);
            return;
        }

        logger.info("Streaming CSV: {}", resourcePath);

        try (InputStream inputStream = resource.getInputStream();
             Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
             CSVReader csvReader = new CSVReader(reader)) {

            String[] headers = csvReader.readNext(); // Read header row
            if (headers == null) {
                logger.warn("Empty CSV file: {}", resourcePath);
                return;
            }

            String[] row;
            int rowCount = 0;
            while ((row = csvReader.readNext()) != null) {
                try {
                    processor.process(headers, row);
                    rowCount++;

                    // Progress logging for large files
                    if (rowCount % 50000 == 0) {
                        logger.debug("Processed {} rows from {}", rowCount, resourcePath);
                    }
                } catch (Exception e) {
                    logger.debug("Error processing row {} in {}: {}", rowCount, resourcePath, e.getMessage());
                }
            }

            logger.info("Successfully streamed {} rows from {}", rowCount, resourcePath);
        }
    }

    @FunctionalInterface
    private interface CsvRowProcessor {
        void process(String[] headers, String[] row);
    }

    private Double parseDouble(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer parseInt(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void linkFareRules() {
        logger.info("Linking fare rules...");
        for (FareRuleDto fr : fareRules) {
            if (fr.getFareId() != null) {
                FareAttributeDto fa = faresById.get(fr.getFareId());
                if (fa != null) {
                    fr.setFareAttribute(fa);
                    fa.addFareRule(fr);
                }
            }
        }
    }

    private void clearMemory() {
        routesById.clear();
        tripsById.clear();
        stopsById.clear();
        faresById.clear();
        fareRules.clear();
        tripToStopSequence.clear();
        stopToTripReferences.clear();
        tripStopTimeMap.clear();
    }

    // --- Getters for services/controllers ---
    public Collection<RouteDto> getAllRoutes() {
        return Collections.unmodifiableCollection(routesById.values());
    }

    public RouteDto getRouteById(String id) {
        return routesById.get(id);
    }

    public TripDto getTripById(String id) {
        return tripsById.get(id);
    }

    public StopDto getStopById(String id) {
        return stopsById.get(id);
    }

    public List<StopTimeDto> getStopTimesForTrip(String tripId) {
        List<String> stopIds = tripToStopSequence.get(tripId);
        if (stopIds == null) return Collections.emptyList();

        Map<String, StopTimeDto> stopTimes = tripStopTimeMap.get(tripId);
        List<StopTimeDto> result = new ArrayList<>();
        for (String stopId : stopIds) {
            StopTimeDto stopTime = stopTimes != null ? stopTimes.get(stopId) : null;
            if (stopTime == null) {
                // Create minimal stop time if not stored
                stopTime = new StopTimeDto();
                stopTime.setTripId(tripId);
                stopTime.setStopId(stopId);
            }
            result.add(stopTime);
        }
        return result;
    }

    public List<StopTimeDto> getStopTimesForStop(String stopId) {
        List<String> tripIds = stopToTripReferences.get(stopId);
        if (tripIds == null) return Collections.emptyList();

        List<StopTimeDto> result = new ArrayList<>();
        for (String tripId : tripIds) {
            Map<String, StopTimeDto> stopTimes = tripStopTimeMap.get(tripId);
            if (stopTimes != null) {
                StopTimeDto stopTime = stopTimes.get(stopId);
                if (stopTime != null) {
                    result.add(stopTime);
                }
            }
        }
        return result;
    }

    public List<FareRuleDto> getFareRulesForRoute(String routeId) {
        List<FareRuleDto> result = new ArrayList<>();
        for (FareRuleDto fr : fareRules) {
            if (routeId.equals(fr.getRouteId())) {
                result.add(fr);
            }
        }
        return result;
    }

    private void printStatistics() {
        logger.info("=== Data Statistics ===");
        logger.info("Routes: {}", routesById.size());
        logger.info("Trips: {}", tripsById.size());
        logger.info("Stops: {}", stopsById.size());
        logger.info("Trip-Stop sequences: {}", tripToStopSequence.size());
        logger.info("Stop-Trip references: {}", stopToTripReferences.size());
        logger.info("Fare Attributes: {}", faresById.size());
        logger.info("Fare Rules: {}", fareRules.size());
        logger.info("Memory Usage: {}", getMemoryUsage());
    }

    public String getMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        long maxMemory = runtime.maxMemory();
        return String.format("Used: %dMB, Max: %dMB",
                usedMemory / (1024 * 1024), maxMemory / (1024 * 1024));
    }
}