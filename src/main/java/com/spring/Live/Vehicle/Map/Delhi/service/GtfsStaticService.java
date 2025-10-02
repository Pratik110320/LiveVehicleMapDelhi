package com.spring.Live.Vehicle.Map.Delhi.service;

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

import java.io.IOException;
import java.io.InputStreamReader;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
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

    @PostConstruct
    public void loadGtfsData() {
        log.info("Loading GTFS static data from: {}", gtfsDataLocation);
        agencies = loadCsvDataToMap("agency.txt", AgencyDto.class, AgencyDto::getAgencyId);
        routes = loadCsvDataToMap("routes.txt", RouteDto.class, RouteDto::getRouteId);
        trips = loadCsvDataToMap("trips.txt", TripDto.class, TripDto::getTripId);
        stops = loadCsvDataToMap("stops.txt", StopDto.class, StopDto::getStopId);
        fareAttributes = loadCsvDataToMap("fare_attributes.txt", FareAttributeDto.class, FareAttributeDto::getFareId);
        calendars = loadCsvDataToMap("calendar.txt", CalendarDto.class, CalendarDto::getServiceId);

        List<FareRuleDto> fareRulesList = loadCsvDataToList("fare_rules.txt", FareRuleDto.class);
        fareRulesMap = new HashMap<>();
        for (FareRuleDto rule : fareRulesList) {
            if (rule.getRouteId() != null && rule.getOrigin_id() != null && rule.getDestination_id() != null) {
                String key = String.join(":", rule.getRouteId(), rule.getOrigin_id(), rule.getDestination_id());
                fareRulesMap.putIfAbsent(key, rule);
            }
        }
        log.info("Processed {} fare rules into a lookup map.", fareRulesMap.size());

        List<StopTimeDto> stopTimes = loadCsvDataToList("stop_times.txt", StopTimeDto.class);
        stopTimesByTripId = stopTimes.stream()
                .filter(st -> st.getTripId() != null && !st.getTripId().isBlank())
                .collect(Collectors.groupingBy(StopTimeDto::getTripId));

        // Pre-calculate which routes serve which stops for fast lookups
        Map<String, Set<String>> routeIdsByStopId = new HashMap<>();
        trips.values().forEach(trip -> {
            List<StopTimeDto> times = stopTimesByTripId.get(trip.getTripId());
            if (times != null) {
                for (StopTimeDto time : times) {
                    routeIdsByStopId.computeIfAbsent(time.getStopId(), k -> new HashSet<>()).add(trip.getRouteId());
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

        this.activeServiceIds = determineActiveServiceIds();
        log.info("Found {} active service IDs for today.", this.activeServiceIds.size());
    }

    private <T> List<T> loadCsvDataToList(String fileName, Class<T> type) {
        String fullPath = gtfsDataLocation + fileName;
        Resource resource = resourceLoader.getResource(fullPath);
        if (!resource.exists()) {
            log.error("GTFS data file not found at path: {}", fullPath);
            return new ArrayList<>();
        }
        try (InputStreamReader reader = new InputStreamReader(resource.getInputStream())) {
            List<T> list = new CsvToBeanBuilder<T>(reader)
                    .withType(type)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build()
                    .parse();
            log.info("Successfully loaded {} records from {}", list.size(), fileName);
            return list;
        } catch (IOException e) {
            log.error("Error loading list data from " + fileName, e);
            return new ArrayList<>();
        }
    }

    private <T> Map<String, T> loadCsvDataToMap(String fileName, Class<T> type, Function<T, String> keyExtractor) {
        try {
            return loadCsvDataToList(fileName, type).stream()
                    .filter(item -> keyExtractor.apply(item) != null)
                    .collect(Collectors.toMap(keyExtractor, Function.identity(), (first, second) -> first));
        } catch (Exception e) {
            log.error("Error loading map data from " + fileName, e);
            return new HashMap<>();
        }
    }

    private Set<String> determineActiveServiceIds() {
        Set<String> activeIds = new HashSet<>();
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Kolkata"));
        DayOfWeek dayOfWeek = today.getDayOfWeek();

        for (CalendarDto calendar : calendars.values()) {
            try {
                LocalDate startDate = LocalDate.parse(calendar.getStartDate(), DateTimeFormatter.ofPattern("yyyyMMdd"));
                LocalDate endDate = LocalDate.parse(calendar.getEndDate(), DateTimeFormatter.ofPattern("yyyyMMdd"));

                if (today.isBefore(startDate) || today.isAfter(endDate)) {
                    continue;
                }

                boolean runsToday = switch (dayOfWeek) {
                    case MONDAY -> calendar.isMonday();
                    case TUESDAY -> calendar.isTuesday();
                    case WEDNESDAY -> calendar.isWednesday();
                    case THURSDAY -> calendar.isThursday();
                    case FRIDAY -> calendar.isFriday();
                    case SATURDAY -> calendar.isSaturday();
                    case SUNDAY -> calendar.isSunday();
                };

                if (runsToday) {
                    activeIds.add(calendar.getServiceId());
                }
            } catch (Exception e) {
                log.error("Could not parse date for service_id: {}", calendar.getServiceId(), e);
            }
        }
        return activeIds;
    }

    public Collection<RouteDto> getActiveRoutes() {
        Set<String> activeRouteIds = trips.values().stream()
                .filter(trip -> activeServiceIds.contains(trip.getServiceId()))
                .map(TripDto::getRouteId)
                .collect(Collectors.toSet());

        return routes.values().stream()
                .filter(route -> activeRouteIds.contains(route.getRouteId()))
                .collect(Collectors.toList());
    }

    public Collection<StopDto> getAllStops() {
        return stops.values();
    }

    public List<RouteDto> getRoutesForStop(String stopId) {
        return routesByStopId.getOrDefault(stopId, Collections.emptyList());
    }

    public Map<String, RouteDto> getRoutes() { return routes; }
    public Map<String, TripDto> getTrips() { return trips; }

    public List<StopDto> getStopsByRouteId(String routeId) {
        // Find a representative trip for the given route that is active today
        Optional<TripDto> representativeTrip = trips.values().stream()
                .filter(trip -> routeId.equals(trip.getRouteId()) && activeServiceIds.contains(trip.getServiceId()))
                .findFirst();

        if (representativeTrip.isEmpty()) {
            return Collections.emptyList();
        }

        // Get stop times for that trip and sort them by sequence
        List<StopTimeDto> stopTimesForTrip = new ArrayList<>(stopTimesByTripId.getOrDefault(representativeTrip.get().getTripId(), Collections.emptyList()));
        stopTimesForTrip.sort(Comparator.comparingInt(StopTimeDto::getStopSequence));

        // Map the sorted stop times to StopDto objects to get their details (including lat/lon)
        return stopTimesForTrip.stream()
                .map(stopTime -> stops.get(stopTime.getStopId()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public List<ScheduleItemDto> getScheduleForTrip(String tripId) {
        List<StopTimeDto> stopTimesForTrip = stopTimesByTripId.get(tripId);
        if (stopTimesForTrip == null || stopTimesForTrip.isEmpty()) {
            return Collections.emptyList();
        }

        stopTimesForTrip.sort(Comparator.comparingInt(StopTimeDto::getStopSequence));

        return stopTimesForTrip.stream()
                .map(stopTime -> {
                    StopDto stop = stops.get(stopTime.getStopId());
                    return stop != null ? new ScheduleItemDto(stop, stopTime) : null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public List<StopDto> searchStopsByName(String query) {
        if (query == null || query.trim().isEmpty() || query.length() < 3) {
            return Collections.emptyList();
        }
        String lowerCaseQuery = query.toLowerCase();
        return stops.values().stream()
                .filter(stop -> stop.getStopName().toLowerCase().contains(lowerCaseQuery))
                .limit(50)
                .collect(Collectors.toList());
    }

    public FareAttributeDto getFare(String routeId, String fromStopId, String toStopId) {
        String key = String.join(":", routeId, fromStopId, toStopId);
        FareRuleDto matchingRule = fareRulesMap.get(key);
        if (matchingRule != null) {
            return fareAttributes.get(matchingRule.getFareId());
        }
        return null;
    }
}

