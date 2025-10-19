package com.spring.Live.Vehicle.Map.Delhi.model;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import com.spring.Live.Vehicle.Map.Delhi.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Component
public class DataLoader implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataLoader.class);
    private static final int BATCH_SIZE = 500; // Define a batch size

    @Autowired private AgencyRepository agencyRepository;
    @Autowired private RouteRepository routeRepository;
    @Autowired private StopRepository stopRepository;
    @Autowired private TripRepository tripRepository;
    @Autowired private StopTimeRepository stopTimeRepository;
    @Autowired private CalendarRepository calendarRepository;
    @Autowired private FareAttributeRepository fareAttributeRepository;
    @Autowired private FareRuleRepository fareRuleRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (agencyRepository.count() > 0) {
            logger.info("GTFS data is already present. Skipping data loading.");
            return;
        }

        logger.info("Starting GTFS data loading process...");

        // Load single, smaller files directly
        loadCsvData("static/agency.csv", agencyRepository, this::parseAgency);
        loadCsvData("static/calendar.csv", calendarRepository, this::parseCalendar);

        // Load larger files in batches
        loadCsvData("static/routes.csv", routeRepository, this::parseRoute);
        loadCsvData("static/stops.csv", stopRepository, this::parseStop);
        loadCsvData("static/trips.csv", tripRepository, this::parseTrip);

        // Load partitioned files
        String[] stopTimeFiles = {"aa", "ab", "ac", "ad", "ae", "af", "ag", "ah"};
        for (String suffix : stopTimeFiles) {
            loadCsvData("static/stop_times_part_" + suffix + ".csv", stopTimeRepository, this::parseStopTime);
        }

        String[] fareAttrFiles = {"aa", "ab", "ac", "ad", "ae"};
        for (String suffix : fareAttrFiles) {
            loadCsvData("static/fare_attributes_part_" + suffix + ".csv", fareAttributeRepository, this::parseFareAttribute);
        }

        String[] fareRuleFiles = {"aa", "ab", "ac", "ad", "ae"};
        for (String suffix : fareRuleFiles) {
            loadCsvData("static/fare_rules_part_" + suffix + ".csv", fareRuleRepository, this::parseFareRule);
        }

        logger.info("GTFS data loading complete.");
    }

    private <T> void loadCsvData(String resourcePath, JpaRepository<T, ?> repository, Function<String[], T> parser) {
        logger.info("Loading data from {}...", resourcePath);
        List<T> batchList = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new InputStreamReader(new ClassPathResource(resourcePath).getInputStream()))) {
            reader.readNext(); // Skip header row
            String[] line;
            while ((line = reader.readNext()) != null) {
                T entity = parser.apply(line);
                if (entity != null) {
                    batchList.add(entity);
                }
                if (batchList.size() >= BATCH_SIZE) {
                    repository.saveAll(batchList);
                    batchList.clear();
                }
            }
            if (!batchList.isEmpty()) {
                repository.saveAll(batchList);
            }
        } catch (IOException | CsvValidationException e) {
            logger.error("Failed to load data from " + resourcePath, e);
        }
        logger.info("Finished loading data from {}.", resourcePath);
    }

    // --- CSV Parser Methods ---

    private Agency parseAgency(String[] line) {
        try {
            Agency agency = new Agency();
            agency.setAgencyId(line[0]);
            agency.setAgencyName(line[1]);
            agency.setAgencyUrl(line[2]);
            agency.setAgencyTimezone(line[3]);
            agency.setAgencyLang(line[4]);
            return agency;
        } catch (Exception e) {
            logger.warn("Skipping malformed agency row: {}", String.join(",", line));
            return null;
        }
    }

    private Route parseRoute(String[] line) {
        try {
            Route route = new Route();
            route.setRouteId(line[0]);
            route.setAgencyId(line[1]);
            route.setRouteShortName(line[2]);
            route.setRouteLongName(line[3]);
            route.setRouteType(Integer.parseInt(line[4]));
            return route;
        } catch (Exception e) {
            logger.warn("Skipping malformed route row: {}", String.join(",", line));
            return null;
        }
    }

    private Stop parseStop(String[] line) {
        try {
            Stop stop = new Stop();
            stop.setStopId(line[0]);
            stop.setStopCode(line[1]);
            stop.setStopName(line[2]);
            stop.setStopLat(Double.parseDouble(line[3]));
            stop.setStopLon(Double.parseDouble(line[4]));
            return stop;
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            logger.warn("Skipping malformed stop row: {} - Error: {}", String.join(",", line), e.getMessage());
            return null;
        }
    }

    private Trip parseTrip(String[] line) {
        try {
            Trip trip = new Trip();
            trip.setRouteId(line[0]);
            trip.setServiceId(line[1]);
            trip.setTripId(line[2]);
            trip.setShapeId(line[5]);
            return trip;
        } catch (Exception e) {
            logger.warn("Skipping malformed trip row: {}", String.join(",", line));
            return null;
        }
    }

    private StopTime parseStopTime(String[] line) {
        try {
            StopTime stopTime = new StopTime();
            stopTime.setTripId(line[0]);
            stopTime.setArrivalTime(line[1]);
            stopTime.setDepartureTime(line[2]);
            stopTime.setStopId(line[3]);
            stopTime.setStopSequence(Integer.parseInt(line[4]));
            return stopTime;
        } catch (Exception e) {
            logger.warn("Skipping malformed stop_time row: {}", String.join(",", line));
            return null;
        }
    }

    private Calendar parseCalendar(String[] line) {
        try {
            Calendar calendar = new Calendar();
            calendar.setServiceId(line[0]);
            calendar.setMonday(Integer.parseInt(line[1]));
            calendar.setTuesday(Integer.parseInt(line[2]));
            calendar.setWednesday(Integer.parseInt(line[3]));
            calendar.setThursday(Integer.parseInt(line[4]));
            calendar.setFriday(Integer.parseInt(line[5]));
            calendar.setSaturday(Integer.parseInt(line[6]));
            calendar.setSunday(Integer.parseInt(line[7]));
            calendar.setStartDate(line[8]);
            calendar.setEndDate(line[9]);
            return calendar;
        } catch (Exception e) {
            logger.warn("Skipping malformed calendar row: {}", String.join(",", line));
            return null;
        }
    }

    private FareAttribute parseFareAttribute(String[] line) {
        try {
            FareAttribute fareAttribute = new FareAttribute();
            fareAttribute.setFareId(line[0]);
            fareAttribute.setCurrencyType(line[2]);
            fareAttribute.setPaymentMethod(Integer.parseInt(line[3]));
            fareAttribute.setTransfers(Integer.parseInt(line[4]));
            return fareAttribute;
        } catch (Exception e) {
            logger.warn("Skipping malformed fare_attribute row: {}", String.join(",", line));
            return null;
        }
    }

    private FareRule parseFareRule(String[] line) {
        try {
            FareRule fareRule = new FareRule();
            fareRule.setFareId(line[0]);
            fareRule.setRouteId(line[1]);
            return fareRule;
        } catch (Exception e) {
            logger.warn("Skipping malformed fare_rule row: {}", String.join(",", line));
            return null;
        }
    }
}

