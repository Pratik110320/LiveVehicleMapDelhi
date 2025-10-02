package com.spring.Live.Vehicle.Map.Delhi.service;

import com.spring.Live.Vehicle.Map.Delhi.model.*;
import jakarta.annotation.PostConstruct;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class GtfsStaticService {

    private final Logger log = LoggerFactory.getLogger(GtfsStaticService.class);

    private final Map<String, String> tripToRouteMap = new ConcurrentHashMap<>();
    private final Map<String, String> routeIdToNameMap = new ConcurrentHashMap<>();
    private final Map<String, StopDto> stopsMap = new ConcurrentHashMap<>();
    private final Map<String, AgencyDto> agencyMap = new ConcurrentHashMap<>();
    private final Map<String, CalendarDto> calendarMap = new ConcurrentHashMap<>();
    private final Map<String, FareAttributeDto> fareAttributeMap = new ConcurrentHashMap<>();
    // ** OPTIMIZATION: Use maps for efficient lookups instead of large lists **
    private final Map<String, List<FareRuleDto>> fareRulesByRoute = new ConcurrentHashMap<>();
    private final Map<String, List<StopTimeDto>> stopTimesByTrip = new ConcurrentHashMap<>();


    @PostConstruct
    public void loadStaticData() {
        loadData("agency.txt", this::parseAgency);
        loadData("calendar.txt", this::parseCalendar);
        loadData("fare_attributes.txt", this::parseFareAttribute);
        loadData("fare_rules.txt", this::parseFareRule);
        loadData("stop_times.txt", this::parseStopTime);
        loadData("trips.txt", this::parseTrip);
        loadData("routes.txt", this::parseRoute);
        loadData("stops.txt", this::parseStop);
    }

    private InputStream getInputStreamForFile(String fileName) throws IOException {
        File dockerFile = new File("/app/static/" + fileName);
        if (dockerFile.exists()) {
            log.info("Loading GTFS data for '{}' from Docker volume.", fileName);
            return new FileInputStream(dockerFile);
        }
        log.info("Loading GTFS data for '{}' from classpath.", fileName);
        return new ClassPathResource("static/" + fileName).getInputStream();
    }


    private void loadData(String fileName, CSVRecordProcessor processor) {
        try (Reader reader = new InputStreamReader(getInputStreamForFile(fileName));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader().withTrim().withIgnoreEmptyLines())) {
            for (CSVRecord record : csvParser) {
                processor.process(record);
            }
            log.info("Successfully loaded data from {}.", fileName);
        } catch (Exception e) {
            log.warn("Failed to load or parse GTFS static data from {}. This may be optional.", fileName, e);
        }
    }

    private void parseAgency(CSVRecord record) {
        AgencyDto agency = new AgencyDto();
        agency.setAgencyId(record.get("agency_id"));
        agency.setAgencyName(record.get("agency_name"));
        agency.setAgencyUrl(record.get("agency_url"));
        agency.setAgencyTimezone(record.get("agency_timezone"));
        agencyMap.put(agency.getAgencyId(), agency);
    }

    private void parseCalendar(CSVRecord record) {
        CalendarDto calendar = new CalendarDto();
        calendar.setServiceId(record.get("service_id"));
        calendar.setMonday( "1".equals(record.get("monday")));
        calendar.setTuesday( "1".equals(record.get("tuesday")));
        calendar.setWednesday( "1".equals(record.get("wednesday")));
        calendar.setThursday( "1".equals(record.get("thursday")));
        calendar.setFriday( "1".equals(record.get("friday")));
        calendar.setSaturday( "1".equals(record.get("saturday")));
        calendar.setSunday( "1".equals(record.get("sunday")));
        calendar.setStartDate(record.get("start_date"));
        calendar.setEndDate(record.get("end_date"));
        calendarMap.put(calendar.getServiceId(), calendar);
    }

    private void parseFareAttribute(CSVRecord record) {
        FareAttributeDto fare = new FareAttributeDto();
        fare.setFareId(record.get("fare_id"));
        fare.setPrice(new BigDecimal(record.get("price")));
        fare.setCurrencyType(record.get("currency_type"));
        fare.setPaymentMethod(Integer.parseInt(record.get("payment_method")));
        String transfers = record.get("transfers");
        fare.setTransfers(transfers != null && !transfers.isEmpty() ? Integer.parseInt(transfers) : 0);
        fareAttributeMap.put(fare.getFareId(), fare);
    }

    private void parseFareRule(CSVRecord record) {
        FareRuleDto rule = new FareRuleDto();
        rule.setFareId(record.get("fare_id"));
        String routeId = record.get("route_id");
        rule.setRouteId(routeId);
        rule.setOrigin_id(record.get("origin_id"));
        rule.setDestination_id(record.get("destination_id"));
        if (routeId != null && !routeId.isEmpty()) {
            fareRulesByRoute.computeIfAbsent(routeId, k -> new ArrayList<>()).add(rule);
        }
    }

    private void parseStopTime(CSVRecord record) {
        StopTimeDto stopTime = new StopTimeDto();
        stopTime.setTripId(record.get("trip_id"));
        stopTime.setArrivalTime(record.get("arrival_time"));
        stopTime.setDepartureTime(record.get("departure_time"));
        stopTime.setStopId(record.get("stop_id"));
        stopTime.setStopSequence(Integer.parseInt(record.get("stop_sequence")));
        stopTimesByTrip.computeIfAbsent(stopTime.getTripId(), k -> new ArrayList<>()).add(stopTime);
    }

    private void parseTrip(CSVRecord record) {
        tripToRouteMap.put(record.get("trip_id"), record.get("route_id"));
    }

    private void parseRoute(CSVRecord record) {
        routeIdToNameMap.put(record.get("route_id"), record.get("route_long_name"));
    }

    private void parseStop(CSVRecord record) {
        StopDto stop = new StopDto();
        stop.setStopId(record.get("stop_id"));
        stop.setStopName(record.get("stop_name"));
        stop.setStopLat(Double.parseDouble(record.get("stop_lat")));
        stop.setStopLon(Double.parseDouble(record.get("stop_lon")));
        stopsMap.put(stop.getStopId(), stop);
    }

    public String getRouteIdForTrip(String tripId) { return tripToRouteMap.get(tripId); }
    public String getRouteNameForRoute(String routeId) { return routeIdToNameMap.get(routeId); }
    public List<StopDto> getAllStops() { return new ArrayList<>(stopsMap.values()); }
    public List<AgencyDto> getAllAgencies() { return new ArrayList<>(agencyMap.values()); }
    public List<CalendarDto> getAllCalendars() { return new ArrayList<>(calendarMap.values()); }

    // ** REMOVED: Method to get ALL fare attributes to prevent memory issues **

    public List<FareRuleDto> getFareRulesForRoute(String routeId) {
        return fareRulesByRoute.getOrDefault(routeId, List.of());
    }
    public List<StopTimeDto> getStopTimesForTrip(String tripId) {
        return stopTimesByTrip.getOrDefault(tripId, List.of()).stream()
                .sorted(Comparator.comparingInt(StopTimeDto::getStopSequence))
                .collect(Collectors.toList());
    }

    public List<StopDto> getRouteStopsForTrip(String tripId) {
        return getStopTimesForTrip(tripId).stream()
                .map(stopTime -> stopsMap.get(stopTime.getStopId()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    // ** NEW: Method to get specific fares for a given route **
    public List<FareAttributeDto> getFareAttributesForRoute(String routeId) {
        List<FareRuleDto> rules = getFareRulesForRoute(routeId);
        if (rules.isEmpty()) {
            return List.of();
        }
        return rules.stream()
                .map(FareRuleDto::getFareId)
                .distinct()
                .map(fareAttributeMap::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @FunctionalInterface
    interface CSVRecordProcessor {
        void process(CSVRecord record);
    }
}