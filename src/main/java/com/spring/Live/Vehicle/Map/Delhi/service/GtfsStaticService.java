//package com.spring.Live.Vehicle.Map.Delhi.service;
//
//import com.opencsv.CSVReader;
//import com.spring.Live.Vehicle.Map.Delhi.model.*;
//import jakarta.annotation.PostConstruct;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.core.io.Resource;
//import org.springframework.core.io.ResourceLoader;
//import org.springframework.stereotype.Service;
//
//import java.io.InputStreamReader;
//import java.nio.charset.StandardCharsets;
//import java.time.LocalDate;
//import java.time.LocalTime;
//import java.time.ZoneId;
//import java.time.format.DateTimeFormatter;
//import java.util.*;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.function.BiConsumer;
//import java.util.stream.Collectors;
//
//@Service
//public class GtfsStaticService {
//
//    private static final Logger log = LoggerFactory.getLogger(GtfsStaticService.class);
//    private final DateTimeFormatter tf = DateTimeFormatter.ofPattern("H:mm:ss");
//
//    @Autowired
//    private ResourceLoader resourceLoader;
//
//    @Value("${gtfs.static.data.location}")
//    private String gtfsDataLocation;
//
//    // --- In-Memory Data Stores ---
//    private final Map<String, Agency> agencies = new ConcurrentHashMap<>();
//    private final Map<String, Route> routes = new ConcurrentHashMap<>();
//    private final Map<String, Trip> trips = new ConcurrentHashMap<>();
//    private final Map<String, Stop> stops = new ConcurrentHashMap<>();
//    private final Map<String, FareAttribute> fareAttributes = new ConcurrentHashMap<>();
//    private final Map<String, Calendar> calendars = new ConcurrentHashMap<>();
//    private final Map<LocalDate, List<CalendarDate>> calendarExceptions = new ConcurrentHashMap<>();
//
//
//    // --- Memory-Optimized Lookups for Large Files ---
//    private final Map<String, List<StopTime>> stopTimesByTripId = new ConcurrentHashMap<>();
//    private final Map<String, FareRule> fareRulesMap = new ConcurrentHashMap<>();
//    private final Map<String, List<Route>> routesByStopId = new ConcurrentHashMap<>();
//    private final Map<String, Set<String>> tripsByStopId = new ConcurrentHashMap<>();
//
//    private Set<String> activeServiceIds;
//
//    @PostConstruct
//    public void loadGtfsData() {
//        log.info("Loading GTFS static data from: {}", gtfsDataLocation);
//        try {
//            // Load smaller files first
//            streamCsvFile("agency.txt", this::processAgencyRow);
//            streamCsvFile("calendar.txt", this::processCalendarRow);
//            streamCsvFile("calendar_dates.txt", this::processCalendarDateRow);
//            streamCsvFile("routes.txt", this::processRouteRow);
//            streamCsvFile("stops.txt", this::processStopRow);
//            streamCsvFile("fare_attribute_mini.txt", this::processFareAttributeRow);
//
//            // Load trips before stop_times and fare_rules which depend on them
//            streamCsvFile("trips.txt", this::processTripRow);
//
//            // Stream and process the largest files last
//            streamCsvFile("stop_times.txt", this::processStopTimeRow);
//            streamCsvFile("fare_rules.txt", this::processFareRuleRow);
//
//            // Post-processing: Build caches and link data
//            buildStopToRouteLookup();
//            this.activeServiceIds = determineActiveServiceIds();
//
//            log.info("GTFS data loading complete. Found {} active service IDs for today.", this.activeServiceIds.size());
//
//        } catch (Exception e) {
//            log.error("Failed to load GTFS static data. The application might not function correctly.", e);
//        }
//    }
//
//    //region CSV Row Processors
//    private void processAgencyRow(String[] headers, String[] row) {
//        Agency agency = new Agency();
//        agency.setAgencyId(getString(row, 0));
//        agency.setAgencyName(getString(row, 1));
//        agencies.put(agency.getAgencyId(), agency);
//    }
//
//    private void processCalendarRow(String[] headers, String[] row) {
//        Calendar calendar = new Calendar();
//        calendar.setServiceId(getString(row, 0));
//        calendar.setMonday("1".equals(getString(row, 1)));
//        calendar.setTuesday("1".equals(getString(row, 2)));
//        calendar.setWednesday("1".equals(getString(row, 3)));
//        calendar.setThursday("1".equals(getString(row, 4)));
//        calendar.setFriday("1".equals(getString(row, 5)));
//        calendar.setSaturday("1".equals(getString(row, 6)));
//        calendar.setSunday("1".equals(getString(row, 7)));
//        calendar.setStartDate(getString(row, 8));
//        calendar.setEndDate(getString(row, 9));
//        calendars.put(calendar.getServiceId(), calendar);
//    }
//
//    private void processCalendarDateRow(String[] headers, String[] row) {
//        CalendarDate exception = new CalendarDate();
//        exception.setServiceId(getString(row, 0));
//        exception.setDate(getString(row, 1));
//        exception.setExceptionType(getInt(row, 2));
//        calendarExceptions.computeIfAbsent(exception.getDate(), k -> new ArrayList<>()).add(exception);
//    }
//
//
//    private void processRouteRow(String[] headers, String[] row) {
//        Route route = new Route();
//        route.setRouteId(getString(row, 0));
//        route.setAgencyId(getString(row, 1));
//        route.setRouteShortName(getString(row, 2));
//        route.setRouteLongName(getString(row, 3));
//        routes.put(route.getRouteId(), route);
//    }
//
//    private void processStopRow(String[] headers, String[] row) {
//        Stop stop = new Stop();
//        stop.setStopId(getString(row, 0));
//        stop.setStopName(getString(row, 2));
//        stop.setStopLat(getDouble(row, 4));
//        stop.setStopLon(getDouble(row, 5));
//        stops.put(stop.getStopId(), stop);
//    }
//
//    private void processFareAttributeRow(String[] headers, String[] row) {
//        FareAttribute fa = new FareAttribute();
//        fa.setFareId(getString(row, 0));
//        // Add other fields if needed
//        fareAttributes.put(fa.getFareId(), fa);
//    }
//
//    private void processTripRow(String[] headers, String[] row) {
//        Trip trip = new Trip();
//        trip.setRouteId(getString(row, 0));
//        trip.setServiceId(getString(row, 1));
//        trip.setTripId(getString(row, 2));
//        trip.setTripHeadsign(getString(row, 3));
//        trips.put(trip.getTripId(), trip);
//    }
//
//    private void processStopTimeRow(String[] headers, String[] row) {
//        StopTime st = new StopTime();
//        st.setTripId(getString(row, 0));
//        st.setArrivalTime(getString(row, 1));
//        st.setDepartureTime(getString(row, 2));
//        st.setStopId(getString(row, 3));
//        st.setStopSequence(getInt(row, 4));
//        stopTimesByTripId.computeIfAbsent(st.getTripId(), k -> new ArrayList<>()).add(st);
//
//        if (st.getStopId() != null && st.getTripId() != null) {
//            tripsByStopId.computeIfAbsent(st.getStopId(), k -> new HashSet<>()).add(st.getTripId());
//        }
//    }
//
//    private void processFareRuleRow(String[] headers, String[] row) {
//        FareRule rule = new FareRule();
//        rule.setFareId(getString(row, 0));
//        rule.setRouteId(getString(row, 1));
//        rule.setOriginId(getString(row, 2));
//        rule.setDestinationId(getString(row, 3));
//        if (rule.getRouteId() != null && rule.getOriginId() != null && rule.getDestinationId() != null) {
//            String key = String.join(":", rule.getRouteId(), rule.getOriginId(), rule.getDestinationId());
//            fareRulesMap.putIfAbsent(key, rule);
//        }
//    }
//    //endregion
//
//    private void buildStopToRouteLookup() {
//        Map<String, Set<String>> routeIdsByStopId = new HashMap<>();
//        trips.values().forEach(trip -> {
//            if (trip != null && trip.getTripId() != null) {
//                List<StopTime> times = stopTimesByTripId.get(trip.getTripId());
//                if (times != null) {
//                    for (StopTime time : times) {
//                        if (time != null && time.getStopId() != null) {
//                            routeIdsByStopId.computeIfAbsent(time.getStopId(), k -> new HashSet<>()).add(trip.getRouteId());
//                        }
//                    }
//                }
//            }
//        });
//
//        routeIdsByStopId.forEach((stopId, routeIds) -> {
//            List<Route> routeList = routeIds.stream()
//                    .map(routes::get)
//                    .filter(Objects::nonNull)
//                    .sorted(Comparator.comparing(Route::getRouteShortName, Comparator.nullsLast(String::compareTo)))
//                    .collect(Collectors.toList());
//            routesByStopId.put(stopId, routeList);
//        });
//        log.info("Built stop-to-route lookup map for {} stops.", routesByStopId.size());
//    }
//
//    private Set<String> determineActiveServiceIds() {
//        Set<String> activeIds = new HashSet<>();
//        // CRITICAL FIX: Use the agency's timezone to determine the correct date.
//        LocalDate today = LocalDate.now(ZoneId.of("Asia/Kolkata"));
//        log.info("Determining active services for date: {}", today);
//
//
//        // 1. Regular schedule from calendar.txt
//        for (Calendar calendar : calendars.values()) {
//            if (calendar.isActiveOn(today)) {
//                activeIds.add(calendar.getServiceId());
//            }
//        }
//
//        // 2. Apply exceptions from calendar_dates.txt
//        List<CalendarDate> todayExceptions = calendarExceptions.getOrDefault(today, Collections.emptyList());
//        for (CalendarDate exception : todayExceptions) {
//            if (exception.getExceptionType() == 1) { // Service added
//                activeIds.add(exception.getServiceId());
//            } else if (exception.getExceptionType() == 2) { // Service removed
//                activeIds.remove(exception.getServiceId());
//            }
//        }
//        return activeIds;
//    }
//
//
//    //region Public Data Accessors
//
//    public Set<String> getActiveServiceIds() {
//        return this.activeServiceIds;
//    }
//
//    public Collection<Route> getAllRoutes() {
//        return routes.values();
//    }
//
//    public Route getRouteById(String routeId) {
//        return routes.get(routeId);
//    }
//
//    public Collection<Route> getActiveRoutes(String query) {
//        Set<String> activeRouteIds = trips.values().stream()
//                .filter(trip -> this.activeServiceIds.contains(trip.getServiceId()))
//                .map(Trip::getRouteId)
//                .collect(Collectors.toSet());
//
//        var routeStream = routes.values().stream()
//                .filter(route -> activeRouteIds.contains(route.getRouteId()));
//
//        if (query != null && !query.isBlank()) {
//            String qlc = query.toLowerCase();
//            routeStream = routeStream.filter(r -> (r.getRouteLongName() != null && r.getRouteLongName().toLowerCase().contains(qlc)) ||
//                    (r.getRouteShortName() != null && r.getRouteShortName().toLowerCase().contains(qlc)));
//        }
//
//        return routeStream.collect(Collectors.toList());
//    }
//
//    public Collection<Stop> getAllStops() {
//        return stops.values();
//    }
//
//    public Stop getStopById(String stopId) {
//        return stops.get(stopId);
//    }
//
//    public Trip getTripById(String tripId) {
//        return trips.get(tripId);
//    }
//
//    public List<Route> getRoutesForStop(String stopId) {
//        return routesByStopId.getOrDefault(stopId, Collections.emptyList());
//    }
//
//    public List<Stop> getStopsByRouteId(String routeId) {
//        Optional<Trip> representativeTrip = trips.values().stream()
//                .filter(trip -> routeId.equals(trip.getRouteId()))
//                .filter(trip -> this.activeServiceIds.contains(trip.getServiceId()))
//                .findFirst();
//
//        if (representativeTrip.isEmpty()) return Collections.emptyList();
//
//        String tripId = representativeTrip.get().getTripId();
//        List<StopTime> stopTimesForTrip = new ArrayList<>(stopTimesByTripId.getOrDefault(tripId, Collections.emptyList()));
//        stopTimesForTrip.sort(Comparator.comparingInt(StopTime::getStopSequence));
//
//        return stopTimesForTrip.stream()
//                .map(stopTime -> stops.get(stopTime.getStopId()))
//                .filter(Objects::nonNull)
//                .collect(Collectors.toList());
//    }
//
//    public List<ScheduleItem> getScheduleForTrip(String tripId) {
//        List<StopTime> stopTimesForTrip = stopTimesByTripId.get(tripId);
//        if (stopTimesForTrip == null || stopTimesForTrip.isEmpty()) return Collections.emptyList();
//
//        stopTimesForTrip.sort(Comparator.comparingInt(StopTime::getStopSequence));
//
//        return stopTimesForTrip.stream()
//                .map(stopTime -> {
//                    Stop stop = stops.get(stopTime.getStopId());
//                    return stop != null ? new ScheduleItem(stop, stopTime) : null;
//                })
//                .filter(Objects::nonNull)
//                .collect(Collectors.toList());
//    }
//
//    public List<Stop> searchStopsByName(String query) {
//        if (query == null || query.trim().length() < 3) return Collections.emptyList();
//        String lowerCaseQuery = query.toLowerCase();
//        return stops.values().stream()
//                .filter(stop -> stop.getStopName() != null && stop.getStopName().toLowerCase().contains(lowerCaseQuery))
//                .limit(50)
//                .collect(Collectors.toList());
//    }
//
//    public FareAttribute getFare(String routeId, String fromStopId, String toStopId) {
//        String key = String.join(":", routeId, fromStopId, toStopId);
//        FareRule matchingRule = fareRulesMap.get(key);
//        if (matchingRule != null) {
//            return fareAttributes.get(matchingRule.getFareId());
//        }
//        return null;
//    }
//
//    public List<StopTime> getArrivals(String stopId, String fromTimeStr, String toTimeStr) {
//        Set<String> tripIdsForStop = tripsByStopId.getOrDefault(stopId, Collections.emptySet());
//        if (tripIdsForStop.isEmpty()) {
//            return Collections.emptyList();
//        }
//
//        List<StopTime> arrivals = tripIdsForStop.stream()
//                .map(this::getTripById)
//                .filter(Objects::nonNull)
//                .filter(trip -> this.activeServiceIds.contains(trip.getServiceId()))
//                .flatMap(trip -> stopTimesByTripId.getOrDefault(trip.getTripId(), Collections.emptyList()).stream())
//                .filter(st -> stopId.equals(st.getStopId()))
//                .collect(Collectors.toList());
//
//        Comparator<StopTime> byArrivalTime = Comparator.comparing(st -> {
//            try {
//                String time = st.getArrivalTime();
//                if (time != null && time.startsWith("24:")) {
//                    time = "00" + time.substring(2);
//                } else if (time != null && time.startsWith("25:")) {
//                    time = "01" + time.substring(2);
//                }
//                return LocalTime.parse(time, tf);
//            } catch (Exception e) {
//                return LocalTime.MAX;
//            }
//        });
//
//        if ((fromTimeStr == null || fromTimeStr.isEmpty()) && (toTimeStr == null || toTimeStr.isEmpty())) {
//            return arrivals.stream().sorted(byArrivalTime).collect(Collectors.toList());
//        }
//
//        LocalTime from = fromTimeStr == null ? LocalTime.MIN : LocalTime.parse(fromTimeStr, tf);
//        LocalTime to = toTimeStr == null ? LocalTime.MAX : LocalTime.parse(toTimeStr, tf);
//
//        return arrivals.stream()
//                .filter(st -> {
//                    try {
//                        String timeStr = st.getArrivalTime();
//                        if (timeStr != null && timeStr.startsWith("24:")) {
//                            timeStr = "00" + timeStr.substring(2);
//                        } else if (timeStr != null && timeStr.startsWith("25:")) {
//                            timeStr = "01" + timeStr.substring(2);
//                        }
//                        LocalTime arr = LocalTime.parse(timeStr, tf);
//                        return !arr.isBefore(from) && !arr.isAfter(to);
//                    } catch (Exception e) {
//                        return false;
//                    }
//                })
//                .sorted(byArrivalTime)
//                .collect(Collectors.toList());
//    }
//
//    //endregion
//
//    //region CSV Streaming Utility
//    private void streamCsvFile(String fileName, BiConsumer<String[], String[]> rowProcessor) throws Exception {
//        String fullPath = gtfsDataLocation + fileName;
//        Resource resource = resourceLoader.getResource(fullPath);
//        if (!resource.exists()) {
//            log.warn("GTFS data file not found at path: {}. Skipping.", fullPath);
//            return;
//        }
//
//        log.info("Streaming GTFS data from: {}", fileName);
//        long rowCount = 0;
//        try (CSVReader csvReader = new CSVReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
//            String[] headers = csvReader.readNext();
//            if (headers == null) {
//                log.warn("Empty CSV file: {}", fileName);
//                return;
//            }
//            String[] row;
//            while ((row = csvReader.readNext()) != null) {
//                try {
//                    rowProcessor.accept(headers, row);
//                    rowCount++;
//                } catch (Exception e) {
//                    log.debug("Error processing row {} in {}: {}", rowCount, fileName, e.getMessage());
//                }
//            }
//        }
//        log.info("Successfully streamed {} data rows from {}", rowCount, fileName);
//    }
//
//    private String getString(String[] row, int index) {
//        if (row != null && index < row.length) {
//            // Remove BOM character if present at the beginning of the first column
//            if (index == 0 && row[index] != null && row[index].startsWith("\uFEFF")) {
//                return row[index].substring(1);
//            }
//            return row[index];
//        }
//        return null;
//    }
//
//
//    private double getDouble(String[] row, int index) {
//        try {
//            return (row != null && index < row.length) ? Double.parseDouble(row[index]) : 0.0;
//        } catch (NumberFormatException | NullPointerException e) {
//            return 0.0;
//        }
//    }
//
//    private int getInt(String[] row, int index) {
//        try {
//            return (row != null && index < row.length) ? Integer.parseInt(row[index]) : 0;
//        } catch (NumberFormatException | NullPointerException e) {
//            return 0;
//        }
//    }
//    //endregion
//}
//
