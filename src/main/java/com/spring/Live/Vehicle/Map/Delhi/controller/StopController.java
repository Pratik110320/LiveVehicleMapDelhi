package com.spring.Live.Vehicle.Map.Delhi.controller;


import com.spring.Live.Vehicle.Map.Delhi.model.*;
import com.spring.Live.Vehicle.Map.Delhi.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors; // Import collectors

@RestController
@RequestMapping("/api/stops")
public class StopController {

    @Autowired
    private StopRepository stopRepository;

    // --- PERFORMANCE OPTIMIZATION ---
    // Removed unused repositories (Trip, Route, Calendar) as the new
    // optimized query will be handled entirely by StopTimeRepository.
    // @Autowired
    // private TripRepository tripRepository;
    //
    // @Autowired
    // private RouteRepository routeRepository;

    @Autowired
    private StopTimeRepository stopTimeRepository;


    // @Autowired
    // private CalendarRepository calendarRepository;

    @GetMapping("/all")
    public List<Stop> getAllStops() {
        return stopRepository.findAll();
    }

    @GetMapping("/visible")
    public List<Stop> getVisibleStops(
            @RequestParam double minLat,
            @RequestParam double maxLat,
            @RequestParam double minLon,
            @RequestParam double maxLon) {
        // This query now efficiently finds only the stops in the current view.
        return stopRepository.findByStopLatBetweenAndStopLonBetween(minLat, maxLat, minLon, maxLon);
    }

    @GetMapping("/{stopId}/buses")
    public List<StopTime> getBusesAtStop(@PathVariable String stopId) {
        // This endpoint seems to be unused by the frontend, but we'll leave it.
        // For performance, an index on stop_times(stop_id) is critical.
        List<StopTime> result = stopTimeRepository.findByStopId(stopId);
        return result != null ? result : new ArrayList<>();
    }


    @GetMapping("/search")
    public List<Stop> searchStops(@RequestParam String Name) {
        return stopRepository.findByStopNameContainingIgnoreCase(Name);
    }

    @GetMapping("/{stopId}/routesToday")
    public List<StopBusScheduleDTO> getRoutesAtStopToday(@PathVariable String stopId) {
        LocalDate today = LocalDate.now();
        String todayStr = today.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        // Use Locale.ENGLISH to ensure day name is "monday", "tuesday", etc.
        String dayOfWeek = today.getDayOfWeek().toString().toLowerCase(Locale.ENGLISH);

        // --- PERFORMANCE OPTIMIZATION ---
        // This is the single biggest backend improvement.
        // We replaced a massive "N+1 query storm" (where N was the number of stop_times)
        // with a *single*, optimized JPQL query.
        // This query joins StopTime, Trip, Route, and Calendar tables in the database
        // and filters by stopId, date, and day-of-week *all at once*.
        // It directly returns the DTOs we need, minimizing data transfer and processing.
        // This reduces 1 + (N * 3) database queries to just 1.
        List<StopBusScheduleDTO> result = stopTimeRepository.findStopSchedulesForToday(
                stopId,
                todayStr,
                dayOfWeek
        );

        // Old, inefficient logic has been removed.
        // List<StopTime> stopTimes = stopTimeRepository.findByStopId(stopId);
        // ... (removed 40+ lines of looping and multiple repository calls) ...

        // System.out.println("Returning " + result.size() + " bus routes for stop " + stopId);
        return result;
    }

    // --- PERFORMANCE OPTIMIZATION ---
    // This is the new endpoint to solve the N+1 API call problem from the frontend.
    // It gets all stops in the bounds, finds all valid arrivals for them in a
    // SINGLE query, then processes and sorts the results on the server,
    // returning only the top 15.
    @GetMapping("/departures-in-bounds")
    public List<SidebarDepartureDTO> getDeparturesInBounds(
            @RequestParam double minLat,
            @RequestParam double maxLat,
            @RequestParam double minLon,
            @RequestParam double maxLon) {

        // 1. Find all stops within the map bounds.
        List<Stop> visibleStops = stopRepository.findByStopLatBetweenAndStopLonBetween(minLat, maxLat, minLon, maxLon);
        if (visibleStops.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. Get their IDs
        List<String> stopIds = visibleStops.stream().map(Stop::getStopId).collect(Collectors.toList());

        // 3. Get date/day parameters for the query
        LocalDate today = LocalDate.now();
        String todayStr = today.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String dayOfWeek = today.getDayOfWeek().toString().toLowerCase(Locale.ENGLISH);

        // 4. Fetch all valid arrivals for ALL visible stops in a SINGLE query.
        List<SidebarDepartureDTO> allArrivals = stopTimeRepository.findSidebarDepartures(
                stopIds,
                todayStr,
                dayOfWeek
        );

        // 5. Process the results in Java (which is very fast) to find the
        //    soonest arrivals, handling overnight times.
        LocalTime now = LocalTime.now();
        LocalDateTime rightNow = today.atTime(now);

        List<SidebarDepartureDTO> sortedArrivals = allArrivals.stream()
                .map(item -> {
                    try {
                        // Parse "HH:mm:ss" string
                        LocalTime arrival = LocalTime.parse(item.getArrivalTime(), DateTimeFormatter.ofPattern("HH:mm:ss"));
                        LocalDateTime arrivalDt = today.atTime(arrival);

                        // If arrival was earlier today, assume it's for the next day
                        if (arrivalDt.isBefore(rightNow)) {
                            arrivalDt = arrivalDt.plusDays(1);
                        }

                        // Calculate difference in seconds
                        long diffSec = java.time.Duration.between(rightNow, arrivalDt).getSeconds();
                        item.setDiffSec(diffSec);
                        return item;
                    } catch (Exception e) {
                        // Log error for bad time format
                        // log.warn("Invalid arrival time format: {}", item.getArrivalTime());
                        return null; // Skip this invalid record
                    }
                })
                .filter(Objects::nonNull) // Remove any records that failed parsing
                .sorted(Comparator.comparing(SidebarDepartureDTO::getDiffSec)) // Sort by soonest
                .limit(15) // Return only the top 15
                .collect(Collectors.toList());

        return sortedArrivals;
    }

}

