package com.spring.Live.Vehicle.Map.Delhi.service;

import com.spring.Live.Vehicle.Map.Delhi.model.Route;
import com.spring.Live.Vehicle.Map.Delhi.model.Stop;
import com.spring.Live.Vehicle.Map.Delhi.model.StopTime;
import com.spring.Live.Vehicle.Map.Delhi.model.Trip;
import com.spring.Live.Vehicle.Map.Delhi.repository.RouteRepository;
import com.spring.Live.Vehicle.Map.Delhi.repository.StopRepository;
import com.spring.Live.Vehicle.Map.Delhi.repository.StopTimeRepository;
import com.spring.Live.Vehicle.Map.Delhi.repository.TripRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class GtfsStaticService {

    @Autowired
    private StopRepository stopRepository;

    @Autowired
    private RouteRepository routeRepository;

    @Autowired
    private StopTimeRepository stopTimeRepository;

    @Autowired
    private TripRepository tripRepository;


    public List<Stop> getAllStops() {
        return stopRepository.findAll();
    }

    public List<Route> getAllRoutes() {
        return routeRepository.findAll();
    }

    public List<Route> searchRoutes(String query) {
        return routeRepository.findByRouteShortNameContainingOrRouteLongNameContaining(query, query);
    }

    /**
     * Retrieves the schedule for a given stop by linking StopTimes, Trips, and Routes.
     * @param stopId The ID of the stop.
     * @return A sorted list of maps, each containing "routeName" and "arrivalTime".
     */
    public List<Map<String, String>> getScheduleForStop(String stopId) {
        // 1. Find all stop times for the given stop ID.
        List<StopTime> stopTimes = stopTimeRepository.findByStopId(stopId);
        if (stopTimes.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. Extract unique trip IDs from the stop times.
        List<String> tripIds = stopTimes.stream()
                .map(StopTime::getTripId)
                .distinct()
                .collect(Collectors.toList());

        // 3. Find all trips corresponding to these trip IDs.
        List<Trip> trips = tripRepository.findAllById(tripIds);
        Map<String, Trip> tripMap = trips.stream()
                .collect(Collectors.toMap(Trip::getTripId, Function.identity()));

        // 4. Extract unique route IDs from the trips.
        List<String> routeIds = trips.stream()
                .map(Trip::getRouteId)
                .distinct()
                .collect(Collectors.toList());

        // 5. Find all routes corresponding to these route IDs.
        List<Route> routes = routeRepository.findAllById(routeIds);
        Map<String, Route> routeMap = routes.stream()
                .collect(Collectors.toMap(Route::getRouteId, Function.identity()));

        // 6. Build the schedule by combining the data.
        return stopTimes.stream()
                .map(stopTime -> {
                    Trip trip = tripMap.get(stopTime.getTripId());
                    if (trip == null) return null;
                    Route route = routeMap.get(trip.getRouteId());
                    if (route == null) return null;

                    return Map.of(
                            "routeName", route.getRouteShortName(),
                            "arrivalTime", stopTime.getArrivalTime()
                    );
                })
                .filter(scheduleEntry -> scheduleEntry != null)
                .sorted(Comparator.comparing(entry -> entry.get("arrivalTime"))) // Sort by arrival time
                .collect(Collectors.toList());
    }
}

