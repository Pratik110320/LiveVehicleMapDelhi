package com.spring.Live.Vehicle.Map.Delhi.model;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import com.spring.Live.Vehicle.Map.Delhi.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class DataLoader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataLoader.class);

    private final AgencyRepository agencyRepository;
    private final CalendarRepository calendarRepository;
    private final FareAttributeRepository fareAttributeRepository;
    private final FareRuleRepository fareRuleRepository;
    private final RouteRepository routeRepository;
    private final StopRepository stopRepository;
    private final StopTimeRepository stopTimeRepository;
    private final TripRepository tripRepository;

    public DataLoader(AgencyRepository agencyRepository, CalendarRepository calendarRepository,
                      FareAttributeRepository fareAttributeRepository, FareRuleRepository fareRuleRepository,
                      RouteRepository routeRepository, StopRepository stopRepository,
                      StopTimeRepository stopTimeRepository, TripRepository tripRepository) {
        this.agencyRepository = agencyRepository;
        this.calendarRepository = calendarRepository;
        this.fareAttributeRepository = fareAttributeRepository;
        this.fareRuleRepository = fareRuleRepository;
        this.routeRepository = routeRepository;
        this.stopRepository = stopRepository;
        this.stopTimeRepository = stopTimeRepository;
        this.tripRepository = tripRepository;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (agencyRepository.count() > 0) {
            log.info("GTFS static data already loaded. Skipping data loading process.");
            return;
        }

        log.info("Starting GTFS static data load process...");

        loadAgencies();
        loadCalendars();
        loadFareAttributes();
        loadFareRules();
        loadRoutes();
        loadStops();
        loadStopTimes();
        loadTrips();

        log.info("GTFS static data loading complete.");
    }

    // Helper method to read CSV data
    private List<String[]> readCsvData(String filePath) throws IOException, CsvException {
        try (Reader reader = new InputStreamReader(new ClassPathResource(filePath).getInputStream());
             CSVReader csvReader = new CSVReader(reader)) {
            csvReader.skip(1); // Skip header row
            return csvReader.readAll();
        }
    }

    private void loadAgencies() throws IOException, CsvException {
        log.info("Loading agencies...");
        List<Agency> agencies = readCsvData("static/agency.csv").stream().map(row -> {
            Agency agency = new Agency();
            agency.setAgencyId(row[0]);
            agency.setAgencyName(row[1]);
            agency.setAgencyUrl(row[2]);
            agency.setAgencyTimezone(row[3]);
            agency.setAgencyLang(row[4]);
            return agency;
        }).collect(Collectors.toList());
        agencyRepository.saveAll(agencies);
        log.info("Loaded {} agencies.", agencies.size());
    }

    private void loadCalendars() throws IOException, CsvException {
        log.info("Loading calendars...");
        List<Calendar> calendars = readCsvData("static/calendar.csv").stream().map(row -> {
            Calendar calendar = new Calendar();
            calendar.setServiceId(row[0]);
            calendar.setMonday(Integer.parseInt(row[1]));
            calendar.setTuesday(Integer.parseInt(row[2]));
            calendar.setWednesday(Integer.parseInt(row[3]));
            calendar.setThursday(Integer.parseInt(row[4]));
            calendar.setFriday(Integer.parseInt(row[5]));
            calendar.setSaturday(Integer.parseInt(row[6]));
            calendar.setSunday(Integer.parseInt(row[7]));
            calendar.setStartDate(row[8]);
            calendar.setEndDate(row[9]);
            return calendar;
        }).collect(Collectors.toList());
        calendarRepository.saveAll(calendars);
        log.info("Loaded {} calendars.", calendars.size());
    }

    private void loadFareAttributes() throws IOException, CsvException {
        log.info("Loading fare attributes...");
        // Load partitioned files
        char[] partitions = "abcde".toCharArray();
        for (char p : partitions) {
            List<FareAttribute> fareAttributes = readCsvData("static/fare_attributes_part_a" + p + ".csv").stream().map(row -> {
                FareAttribute fa = new FareAttribute();
                fa.setFareId(row[0]);
                fa.setPrice(Double.parseDouble(row[1]));
                fa.setCurrencyType(row[2]);
                fa.setPaymentMethod(Integer.parseInt(row[3]));
                fa.setTransfers(Integer.parseInt(row[4]));
                return fa;
            }).collect(Collectors.toList());
            fareAttributeRepository.saveAll(fareAttributes);
        }
        log.info("Loaded fare attributes.");
    }

    private void loadFareRules() throws IOException, CsvException {
        log.info("Loading fare rules...");
        char[] partitions = "abcde".toCharArray();
        for (char p : partitions) {
            List<FareRule> fareRules = readCsvData("static/fare_rules_part_a" + p + ".csv").stream().map(row -> {
                FareRule fr = new FareRule();
                fr.setFareId(row[0]);
                fr.setRouteId(row[1]);
                return fr;
            }).collect(Collectors.toList());
            fareRuleRepository.saveAll(fareRules);
        }
        log.info("Loaded fare rules.");
    }

    private void loadRoutes() throws IOException, CsvException {
        log.info("Loading routes...");
        List<Route> routes = readCsvData("static/routes.csv").stream().map(row -> {
            Route route = new Route();
            route.setRouteId(row[0]);
            route.setAgencyId(row[1]);
            route.setRouteShortName(row[2]);
            route.setRouteLongName(row[3]);
            route.setRouteType(Integer.parseInt(row[4]));
            return route;
        }).collect(Collectors.toList());
        routeRepository.saveAll(routes);
        log.info("Loaded {} routes.", routes.size());
    }

    private void loadStops() throws IOException, CsvException {
        log.info("Loading stops...");
        List<Stop> stops = readCsvData("static/stops.csv").stream().map(row -> {
            Stop stop = new Stop();
            stop.setStopId(row[0]);
            stop.setStopName(row[1]);
            stop.setStopLat(Double.parseDouble(row[2]));
            stop.setStopLon(Double.parseDouble(row[3]));
            return stop;
        }).collect(Collectors.toList());
        stopRepository.saveAll(stops);
        log.info("Loaded {} stops.", stops.size());
    }

    private void loadStopTimes() throws IOException, CsvException {
        log.info("Loading stop times...");
        char[] partitions = "abcdefgh".toCharArray();
        for (char p : partitions) {
            List<StopTime> stopTimes = readCsvData("static/stop_times_part_a" + p + ".csv").stream().map(row -> {
                StopTime st = new StopTime();
                st.setTripId(row[0]);
                st.setArrivalTime(row[1]);
                st.setDepartureTime(row[2]);
                st.setStopId(row[3]);
                st.setStopSequence(Integer.parseInt(row[4]));
                return st;
            }).collect(Collectors.toList());
            stopTimeRepository.saveAll(stopTimes);
        }
        log.info("Loaded stop times.");
    }

    private void loadTrips() throws IOException, CsvException {
        log.info("Loading trips...");
        List<Trip> trips = readCsvData("static/trips.csv").stream().map(row -> {
            Trip trip = new Trip();
            trip.setRouteId(row[0]);
            trip.setServiceId(row[1]);
            trip.setTripId(row[2]);
            trip.setTripHeadsign(row[3]);
            trip.setDirectionId(Integer.parseInt(row[4]));
            trip.setShapeId(row[5]);
            return trip;
        }).collect(Collectors.toList());
        tripRepository.saveAll(trips);
        log.info("Loaded {} trips.", trips.size());
    }
}
