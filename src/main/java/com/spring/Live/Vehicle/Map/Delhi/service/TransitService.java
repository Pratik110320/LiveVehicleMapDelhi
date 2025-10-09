package com.spring.Live.Vehicle.Map.Delhi.service;

import com.spring.Live.Vehicle.Map.Delhi.model.*;
import com.spring.Live.Vehicle.Map.Delhi.model.Calendar;
import com.spring.Live.Vehicle.Map.Delhi.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TransitService {

    @Autowired
    private StopRepository stopRepository;

    @Autowired
    private RouteRepository routeRepository;

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private StopTimeRepository stopTimeRepository;

    @Autowired
    private CalendarRepository calendarRepository;

    @Autowired
    private FareRuleRepository fareRuleRepository;

    @Autowired
    private FareAttributeRepository fareAttributeRepository;

    // ===============================
    // FEATURE 1: FIND NEAREST STOPS
    // ===============================
    public List<NearbyStopDTO> findNearestStops(double userLat, double userLon, int limitKm) {
        List<Stop> allStops = stopRepository.findAll();

        return allStops.stream()
                .map(stop -> {
                    double distance = calculateDistance(userLat, userLon,
                            stop.getStopLat(), stop.getStopLon());
                    return new NearbyStopDTO(stop, distance);
                })
                .filter(dto -> dto.getDistance() <= limitKm)
                .sorted(Comparator.comparingDouble(NearbyStopDTO::getDistance))
                .limit(10)
                .collect(Collectors.toList());
    }

    // ===============================
    // FEATURE 2: ROUTE PLANNING (A to B)
    // ===============================
    public List<RouteOption> findRoutes(String originStopId, String destStopId) {
        List<RouteOption> options = new ArrayList<>();

        // Find direct routes
        List<RouteOption> directRoutes = findDirectRoutes(originStopId, destStopId);
        options.addAll(directRoutes);

        // Find routes with one transfer (if direct routes < 3)
        if (directRoutes.size() < 3) {
            List<RouteOption> transferRoutes = findRoutesWithTransfer(originStopId, destStopId);
            options.addAll(transferRoutes);
        }

        return options.stream()
                .sorted(Comparator.comparingInt(RouteOption::getTotalStops))
                .limit(5)
                .collect(Collectors.toList());
    }

    private List<RouteOption> findDirectRoutes(String originStopId, String destStopId) {
        List<RouteOption> routes = new ArrayList<>();

        // Get all trips that stop at origin
        List<StopTime> originStops = stopTimeRepository.findByStopId(originStopId);

        for (StopTime originStop : originStops) {
            String tripId = originStop.getTripId();

            // Check if same trip stops at destination
            List<StopTime> destStops = stopTimeRepository
                    .findByTripIdAndStopId(tripId, destStopId);

            for (StopTime destStop : destStops) {
                // Ensure destination comes after origin
                if (destStop.getStopSequence() > originStop.getStopSequence()) {
                    Trip trip = tripRepository.findById(tripId).orElse(null);
                    if (trip != null && isServiceActiveToday(trip.getServiceId())) {
                        Route route = routeRepository.findById(trip.getRouteId()).orElse(null);
                        if (route != null) {
                            routes.add(new RouteOption(
                                    route,
                                    originStop,
                                    destStop,
                                    destStop.getStopSequence() - originStop.getStopSequence()
                            ));
                        }
                    }
                }
            }
        }

        return routes;
    }

    private List<RouteOption> findRoutesWithTransfer(String originStopId, String destStopId) {
        List<RouteOption> routes = new ArrayList<>();

        // Find common transfer points
        Set<String> originReachableStops = getReachableStops(originStopId);
        Set<String> destReachableStops = getReachableStops(destStopId);

        // Find intersection (potential transfer points)
        originReachableStops.retainAll(destReachableStops);

        // Build routes through transfer points (limit to 3)
        int count = 0;
        for (String transferStopId : originReachableStops) {
            if (count >= 3) break;

            List<RouteOption> leg1 = findDirectRoutes(originStopId, transferStopId);
            List<RouteOption> leg2 = findDirectRoutes(transferStopId, destStopId);

            if (!leg1.isEmpty() && !leg2.isEmpty()) {
                // Create combined route (simplified)
                routes.add(leg1.get(0)); // Just add first leg for now
                count++;
            }
        }

        return routes;
    }

    private Set<String> getReachableStops(String stopId) {
        Set<String> reachable = new HashSet<>();
        List<StopTime> stopTimes = stopTimeRepository.findByStopId(stopId);

        for (StopTime st : stopTimes) {
            List<StopTime> tripStops = stopTimeRepository.findByTripIdOrderByStopSequence(st.getTripId());
            tripStops.forEach(ts -> reachable.add(ts.getStopId()));
        }

        return reachable;
    }

    // ===============================
    // FEATURE 3: NEXT BUS ARRIVALS
    // ===============================
    public List<NextBusDTO> getNextBuses(String stopId, int limit) {
        LocalTime now = LocalTime.now();
        List<StopTime> stopTimes = stopTimeRepository.findByStopId(stopId);

        return stopTimes.stream()
                .filter(st -> {
                    Trip trip = tripRepository.findById(st.getTripId()).orElse(null);
                    return trip != null && isServiceActiveToday(trip.getServiceId());
                })
                .filter(st -> isTimeAfter(st.getDepartureTime(), now))
                .sorted(Comparator.comparing(StopTime::getDepartureTime))
                .limit(limit)
                .map(st -> {
                    Trip trip = tripRepository.findById(st.getTripId()).orElse(null);
                    Route route = trip != null ?
                            routeRepository.findById(trip.getRouteId()).orElse(null) : null;
                    return new NextBusDTO(route, st);
                })
                .collect(Collectors.toList());
    }

    // ===============================
    // FEATURE 4: FARE CALCULATION
    // ===============================
    public FareInfo calculateFare(String originStopId, String destStopId, String routeId) {
        Stop origin = stopRepository.findById(originStopId).orElse(null);
        Stop dest = stopRepository.findById(destStopId).orElse(null);

        if (origin == null || dest == null) {
            return new FareInfo(0, "INR", "Stops not found");
        }

        // Find fare rules for this route
        List<FareRule> fareRules = fareRuleRepository.findByRouteId(routeId);

        for (FareRule rule : fareRules) {
            // Check zone-based or origin-destination based fare
            if (matchesFareRule(rule, origin, dest)) {
                FareAttribute fareAttr = fareAttributeRepository
                        .findById(rule.getFareId()).orElse(null);
                if (fareAttr != null) {
                    return new FareInfo(
                            fareAttr.getPrice(),
                            fareAttr.getCurrencyType(),
                            "Standard fare"
                    );
                }
            }
        }

        // Default fare if no specific rule found
        return new FareInfo(10.0f, "INR", "Default fare");
    }

    private boolean matchesFareRule(FareRule rule, Stop origin, Stop dest) {
        // Check if origin/destination zones match
        if (rule.getOriginId() != null && rule.getDestinationId() != null) {
            return rule.getOriginId().equals(origin.getZoneId()) &&
                    rule.getDestinationId().equals(dest.getZoneId());
        }
        return true; // Match all if no specific zones
    }

    // ===============================
    // FEATURE 5: ROUTE DETAILS
    // ===============================
    public RouteDetails getRouteDetails(String routeId) {
        Route route = routeRepository.findById(routeId).orElse(null);
        if (route == null) return null;

        // Get all trips for this route
        List<Trip> trips = tripRepository.findByRouteId(routeId);
        if (trips.isEmpty()) return null;

        // Get stops for first active trip
        Trip activeTrip = trips.stream()
                .filter(t -> isServiceActiveToday(t.getServiceId()))
                .findFirst()
                .orElse(trips.get(0));

        List<StopTime> stopTimes = stopTimeRepository
                .findByTripIdOrderByStopSequence(activeTrip.getTripId());

        List<Stop> stops = stopTimes.stream()
                .map(st -> stopRepository.findById(st.getStopId()).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return new RouteDetails(route, stops, stopTimes);
    }

    // ===============================
    // FEATURE 6: SEARCH FUNCTIONALITY
    // ===============================
    public List<Stop> searchStops(String query) {
        return stopRepository.searchByNameOrCode(query);
    }

    public List<Route> searchRoutes(String query) {
        return routeRepository.searchByName(query);
    }

    // ===============================
    // UTILITY METHODS
    // ===============================
    private boolean isServiceActiveToday(String serviceId) {
        Calendar calendar = calendarRepository.findById(serviceId).orElse(null);
        if (calendar == null) return false;

        DayOfWeek today = LocalDate.now().getDayOfWeek();

        switch (today) {
            case MONDAY: return calendar.isMonday();
            case TUESDAY: return calendar.isTuesday();
            case WEDNESDAY: return calendar.isWednesday();
            case THURSDAY: return calendar.isThursday();
            case FRIDAY: return calendar.isFriday();
            case SATURDAY: return calendar.isSaturday();
            case SUNDAY: return calendar.isSunday();
            default: return false;
        }
    }

    private boolean isTimeAfter(String gtfsTime, LocalTime currentTime) {
        try {
            String[] parts = gtfsTime.split(":");
            int hours = Integer.parseInt(parts[0]);
            int minutes = Integer.parseInt(parts[1]);

            // Handle times past midnight (25:00:00, etc.)
            if (hours >= 24) {
                hours = hours - 24;
            }

            LocalTime scheduleTime = LocalTime.of(hours, minutes);
            return scheduleTime.isAfter(currentTime);
        } catch (Exception e) {
            return false;
        }
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // Haversine formula
        final int R = 6371; // Earth radius in km

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }

    // ===============================
    // DTO CLASSES
    // ===============================
    public static class NearbyStopDTO {
        private Stop stop;
        private double distance;

        public NearbyStopDTO(Stop stop, double distance) {
            this.stop = stop;
            this.distance = distance;
        }

        public Stop getStop() { return stop; }
        public double getDistance() { return distance; }
    }

    public static class RouteOption {
        private Route route;
        private StopTime originStopTime;
        private StopTime destStopTime;
        private int totalStops;

        public RouteOption(Route route, StopTime originStopTime,
                           StopTime destStopTime, int totalStops) {
            this.route = route;
            this.originStopTime = originStopTime;
            this.destStopTime = destStopTime;
            this.totalStops = totalStops;
        }

        public Route getRoute() { return route; }
        public StopTime getOriginStopTime() { return originStopTime; }
        public StopTime getDestStopTime() { return destStopTime; }
        public int getTotalStops() { return totalStops; }
    }

    public static class NextBusDTO {
        private Route route;
        private StopTime stopTime;

        public NextBusDTO(Route route, StopTime stopTime) {
            this.route = route;
            this.stopTime = stopTime;
        }

        public Route getRoute() { return route; }
        public StopTime getStopTime() { return stopTime; }
    }

    public static class FareInfo {
        private float price;
        private String currency;
        private String description;

        public FareInfo(float price, String currency, String description) {
            this.price = price;
            this.currency = currency;
            this.description = description;
        }

        public float getPrice() { return price; }
        public String getCurrency() { return currency; }
        public String getDescription() { return description; }
    }

    public static class RouteDetails {
        private Route route;
        private List<Stop> stops;
        private List<StopTime> stopTimes;

        public RouteDetails(Route route, List<Stop> stops, List<StopTime> stopTimes) {
            this.route = route;
            this.stops = stops;
            this.stopTimes = stopTimes;
        }

        public Route getRoute() { return route; }
        public List<Stop> getStops() { return stops; }
        public List<StopTime> getStopTimes() { return stopTimes; }
    }
}