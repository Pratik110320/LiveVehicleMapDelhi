package com.spring.Live.Vehicle.Map.Delhi.service;

import com.opencsv.bean.CsvToBeanBuilder;
import com.spring.Live.Vehicle.Map.Delhi.model.*;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.io.InputStreamReader;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class GtfsStaticService {

    private Map<String, AgencyDto> agencies;
    private Map<String, RouteDto> routes;
    private Map<String, TripDto> trips;
    private Map<String, StopDto> stops;
    private Map<String, List<StopTimeDto>> stopTimesByTripId;
    private List<FareRuleDto> fareRules;
    private Map<String, FareAttributeDto> fareAttributes;

    @PostConstruct
    public void loadGtfsData() {
        agencies = loadCsvDataToMap("agency.txt", AgencyDto.class, AgencyDto::getAgencyId);
        routes = loadCsvDataToMap("routes.txt", RouteDto.class, RouteDto::getRouteId);
        trips = loadCsvDataToMap("trips.txt", TripDto.class, TripDto::getTripId);
        stops = loadCsvDataToMap("stops.txt", StopDto.class, StopDto::getStopId);
        fareAttributes = loadCsvDataToMap("fare_attributes.txt", FareAttributeDto.class, FareAttributeDto::getFareId);

        // Correctly load fare_rules.txt into a List, as fare_id is not unique
        fareRules = loadCsvDataToList("fare_rules.txt", FareRuleDto.class);

        List<StopTimeDto> stopTimes = loadCsvDataToList("stop_times.txt", StopTimeDto.class);

        stopTimesByTripId = stopTimes.stream().collect(Collectors.groupingBy(StopTimeDto::getTripId));
    }

    private <T> Map<String, T> loadCsvDataToMap(String fileName, Class<T> type, Function<T, String> keyExtractor) {
        try {
            // Using a collector that handles duplicate keys, keeping the first one encountered.
            return loadCsvDataToList(fileName, type).stream()
                    .collect(Collectors.toMap(keyExtractor, Function.identity(), (first, second) -> first));
        } catch (Exception e) {
            System.err.println("Error loading map data from " + fileName);
            e.printStackTrace();
            return new HashMap<>();
        }
    }

    private <T> List<T> loadCsvDataToList(String fileName, Class<T> type) {
        try (InputStreamReader reader = new InputStreamReader(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream(fileName)))) {
            return new CsvToBeanBuilder<T>(reader)
                    .withType(type)
                    .build()
                    .parse();
        } catch (Exception e) {
            System.err.println("Error loading list data from " + fileName);
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public Map<String, RouteDto> getRoutes() {
        return routes;
    }

    public Map<String, TripDto> getTrips() {
        return trips;
    }

    public Collection<RouteDto> getAllRoutes() {
        return routes.values();
    }

    public List<StopDto> getStopsByRouteId(String routeId) {
        // Find a representative trip for the given route
        Optional<TripDto> representativeTrip = trips.values().stream()
                .filter(trip -> routeId.equals(trip.getRouteId()))
                .findFirst();

        if (representativeTrip.isEmpty()) {
            return Collections.emptyList();
        }

        // Get stop times for that trip and sort by sequence
        List<StopTimeDto> stopTimesForTrip = new ArrayList<>(stopTimesByTripId.getOrDefault(representativeTrip.get().getTripId(), Collections.emptyList()));
        stopTimesForTrip.sort(Comparator.comparingInt(StopTimeDto::getStopSequence));

        // Map stop times to stop data
        return stopTimesForTrip.stream()
                .map(stopTime -> stops.get(stopTime.getStopId()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public List<StopDto> searchStopsByName(String query) {
        if (query == null || query.trim().isEmpty()) {
            return Collections.emptyList();
        }
        String lowerCaseQuery = query.toLowerCase();
        return stops.values().stream()
                .filter(stop -> stop.getStopName().toLowerCase().contains(lowerCaseQuery))
                .limit(50) // Limit results for performance
                .collect(Collectors.toList());
    }

    public FareAttributeDto getFare(String routeId, String fromStopId, String toStopId) {
        // Find a fare rule that matches the route, origin, and destination
        // *** FIX: Call .stream() directly on the fareRules list ***
        Optional<FareRuleDto> matchingRule = fareRules.stream()
                .filter(rule -> routeId.equals(rule.getRouteId()) &&
                        fromStopId.equals(rule.getOrigin_id()) &&
                        toStopId.equals(rule.getDestination_id()))
                .findFirst();

        return matchingRule.map(fareRuleDto -> fareAttributes.get(fareRuleDto.getFareId())).orElse(null);
    }
}

