package com.spring.Live.Vehicle.Map.Delhi.service;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.spring.Live.Vehicle.Map.Delhi.model.*;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class GtfsStaticService {

    private static final Logger log = LoggerFactory.getLogger(GtfsStaticService.class);

    @Autowired
    private ResourceLoader resourceLoader;

    @Value("${gtfs.static.data.location}")
    private String gtfsDataLocation;

    private Map<String, AgencyDto> agencies;
    private Map<String, RouteDto> routes;
    private Map<String, TripDto> trips;
    private Map<String, StopDto> stops;
    private Map<String, List<StopTimeDto>> stopTimesByTripId;
    private Map<String, FareRuleDto> fareRulesMap;
    private Map<String, FareAttributeDto> fareAttributes;
    private Map<String, CalendarDto> calendars;
    private Map<String, List<RouteDto>> routesByStopId;
    private Set<String> activeServiceIds;

    private String cleanId(String id) {
        if (id == null) return null;
        if (id.startsWith("\uFEFF")) id = id.substring(1);
        return id.trim();
    }

    @PostConstruct
    public void loadGtfsData() {
        log.info("Loading GTFS static data from: {}", gtfsDataLocation);
        agencies = loadCsvDataToMap("agency.txt", AgencyDto.class, AgencyDto::getAgencyId);
        routes = loadCsvDataToMap("routes.txt", RouteDto.class, RouteDto::getRouteId);
        trips = loadCsvDataToMap("trips.txt", TripDto.class, TripDto::getTripId);
        stops = loadCsvDataToMap("stops.txt", StopDto.class, StopDto::getStopId);
        fareAttributes = loadCsvDataToMap("fare_attribute_mini.txt", FareAttributeDto.class, FareAttributeDto::getFareId);
        calendars = loadCsvDataToMap("calendar.txt", CalendarDto.class, CalendarDto::getServiceId);

        // Stream-process the largest files to avoid OutOfMemoryError
        processFareRules();
        processStopTimes();

        // Pre-calculate which routes serve which stops for fast lookups
        buildStopToRouteLookup();

        this.activeServiceIds = determineActiveServiceIds();
        log.info("Found {} active service IDs for today.", this.activeServiceIds.size());
    }

    /**
     * Streams fare_rules.txt row by row to build the fareRulesMap efficiently.
     */
    private void processFareRules() {
        this.fareRulesMap = new HashMap<>();
        streamCsvData("fare_rules.txt", FareRuleDto.class, rule -> {
            if (rule.getRouteId() != null && rule.getOriginId() != null && rule.getDestinationId() != null) {
                String key = String.join(":", cleanId(rule.getRouteId()), cleanId(rule.getOriginId()), cleanId(rule.getDestinationId()));
                fareRulesMap.putIfAbsent(key, rule);
            }
        });
        log.info("Processed {} fare rules into a lookup map.", fareRulesMap.size());
    }

    /**
     * Streams stop_times.txt row by row to build the stopTimesByTripId map efficiently.
     */
    private void processStopTimes() {
        this.stopTimesByTripId = new HashMap<>();
        streamCsvData("stop_times.txt", StopTimeDto.class, st -> {
            String tripId = cleanId(st.getTripId());
            if (tripId != null && !tripId.isBlank()) {
                stopTimesByTripId.computeIfAbsent(tripId, k -> new ArrayList<>()).add(st);
            }
        });
        log.info("Processed stop_times records into a lookup map with {} trip IDs.", stopTimesByTripId.size());
    }

    private void buildStopToRouteLookup() {
        Map<String, Set<String>> routeIdsByStopId = new HashMap<>();
        trips.values().forEach(trip -> {
            if (trip != null && trip.getTripId() != null) {
                List<StopTimeDto> times = stopTimesByTripId.get(cleanId(trip.getTripId()));
                if (times != null) {
                    for (StopTimeDto time : times) {
                        if (time != null && time.getStopId() != null) {
                            routeIdsByStopId.computeIfAbsent(cleanId(time.getStopId()), k -> new HashSet<>()).add(cleanId(trip.getRouteId()));
                        }
                    }
                }
            }
        });
        routesByStopId = new HashMap<>();
        routeIdsByStopId.forEach((stopId, routeIds) -> {
            List<RouteDto> routeList = routeIds.stream()
                    .map(routes::get)
                    .filter(Objects::nonNull)
                    .sorted(Comparator.comparing(RouteDto::getRouteShortName, Comparator.nullsLast(String::compareTo)))
                    .collect(Collectors.toList());
            routesByStopId.put(stopId, routeList);
        });
        log.info("Built stop-to-route lookup map for {} stops.", routesByStopId.size());
    }

    /**
     * Generic utility to stream a CSV file and apply an action to each parsed bean.
     * This is memory-efficient as it doesn't load the whole file at once.
     */
    private <T> void streamCsvData(String fileName, Class<T> type, Consumer<T> consumer) {
        String fullPath = gtfsDataLocation + fileName;
        Resource resource = resourceLoader.getResource(fullPath);
        if (!resource.exists()) {
            log.warn("GTFS data file not found at path: {}. Skipping.", fullPath);
            return;
        }
        try (InputStreamReader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
            CsvToBean<T> csvToBean = new CsvToBeanBuilder<T>(reader)
                    .withType(type)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();
            csvToBean.iterator().forEachRemaining(consumer);
        } catch (Exception e) {
            log.error("Error streaming data from " + fileName, e);
        }
    }

    private <T> Map<String, T> loadCsvDataToMap(String fileName, Class<T> type, Function<T, String> keyExtractor) {
        Map<String, T> map = new HashMap<>();
        streamCsvData(fileName, type, bean -> {
            String key = cleanId(keyExtractor.apply(bean));
            if (key != null && !key.isBlank()) {
                map.putIfAbsent(key, bean);
            }
        });
        log.info("Successfully loaded {} records into map from {}", map.size(), fileName);
        return map;
    }

    private Set<String> determineActiveServiceIds() {
        if (trips == null || trips.isEmpty()) return Collections.emptySet();
        return trips.values().stream()
                .map(trip -> cleanId(trip.getServiceId()))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    public TripDto getTripById(String tripId) {
        return trips.get(cleanId(tripId));
    }

    public Collection<RouteDto> getActiveRoutes() {
        if (trips == null || activeServiceIds == null) return Collections.emptyList();
        Set<String> activeRouteIds = trips.values().stream()
                .filter(trip -> activeServiceIds.contains(cleanId(trip.getServiceId())))
                .map(trip -> cleanId(trip.getRouteId()))
                .collect(Collectors.toSet());

        if (activeRouteIds.isEmpty() && routes != null && !routes.isEmpty()) {
            log.warn("No active routes found. Displaying all available routes as a fallback.");
            return routes.values();
        }

        return routes.values().stream()
                .filter(route -> activeRouteIds.contains(cleanId(route.getRouteId())))
                .collect(Collectors.toList());
    }

    public Collection<StopDto> getAllStops() {
        return stops != null ? stops.values() : Collections.emptyList();
    }

    public List<RouteDto> getRoutesForStop(String stopId) {
        return routesByStopId.getOrDefault(cleanId(stopId), Collections.emptyList());
    }

    public Map<String, RouteDto> getRoutes() { return routes; }
    public Map<String, TripDto> getTrips() { return trips; }

    public List<StopDto> getStopsByRouteId(String routeId) {
        final String cleanedRouteId = cleanId(routeId);
        Optional<TripDto> representativeTrip = trips.values().stream()
                .filter(trip -> cleanedRouteId.equals(cleanId(trip.getRouteId())))
                .findFirst();

        if (representativeTrip.isEmpty()) return Collections.emptyList();

        String tripId = cleanId(representativeTrip.get().getTripId());
        List<StopTimeDto> stopTimesForTrip = new ArrayList<>(stopTimesByTripId.getOrDefault(tripId, Collections.emptyList()));
        stopTimesForTrip.sort(Comparator.comparingInt(StopTimeDto::getStopSequence));

        return stopTimesForTrip.stream()
                .map(stopTime -> stops.get(cleanId(stopTime.getStopId())))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public List<ScheduleItemDto> getScheduleForTrip(String tripId) {
        List<StopTimeDto> stopTimesForTrip = stopTimesByTripId.get(cleanId(tripId));
        if (stopTimesForTrip == null || stopTimesForTrip.isEmpty()) return Collections.emptyList();

        stopTimesForTrip.sort(Comparator.comparingInt(StopTimeDto::getStopSequence));

        return stopTimesForTrip.stream()
                .map(stopTime -> {
                    StopDto stop = stops.get(cleanId(stopTime.getStopId()));
                    return stop != null ? new ScheduleItemDto(stop, stopTime) : null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public List<StopDto> searchStopsByName(String query) {
        if (query == null || query.trim().length() < 3) return Collections.emptyList();
        String lowerCaseQuery = query.toLowerCase();
        return stops.values().stream()
                .filter(stop -> stop.getStopName() != null && stop.getStopName().toLowerCase().contains(lowerCaseQuery))
                .limit(50)
                .collect(Collectors.toList());
    }

    public FareAttributeDto getFare(String routeId, String fromStopId, String toStopId) {
        String key = String.join(":", cleanId(routeId), cleanId(fromStopId), cleanId(toStopId));
        FareRuleDto matchingRule = fareRulesMap.get(key);
        if (matchingRule != null) {
            return fareAttributes.get(cleanId(matchingRule.getFareId()));
        }
        return null;
    }
}

