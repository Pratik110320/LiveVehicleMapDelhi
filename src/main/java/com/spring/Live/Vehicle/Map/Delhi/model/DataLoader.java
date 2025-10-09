package com.spring.Live.Vehicle.Map.Delhi.model;


import com.opencsv.bean.CsvToBeanBuilder;
import com.spring.Live.Vehicle.Map.Delhi.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Component
public class DataLoader implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataLoader.class);


    private static final Map<Class<?>, Function<Object, String>> ENTITYId_GETTERS = Map.of(
            Agency.class, entity -> ((Agency) entity).getAgencyId(),
            Calendar.class, entity -> ((Calendar) entity).getServiceId(),
            FareAttribute.class, entity -> ((FareAttribute) entity).getFareId(),
            Route.class, entity -> ((Route) entity).getRouteId(),
            Stop.class, entity -> ((Stop) entity).getStopId(),
            Trip.class, entity -> ((Trip) entity).getTripId()
    );

    private final AgencyRepository agencyRepository;
    private final CalendarRepository calendarRepository;
    private final FareAttributeRepository fareAttributeRepository;
    private final FareRuleRepository fareRuleRepository;
    private final RouteRepository routeRepository;
    private final TripRepository tripRepository;
    private final StopRepository stopRepository;
    private final StopTimeRepository stopTimeRepository;

    public DataLoader(AgencyRepository agencyRepository, CalendarRepository calendarRepository, FareAttributeRepository fareAttributeRepository, FareRuleRepository fareRuleRepository, RouteRepository routeRepository, TripRepository tripRepository, StopRepository stopRepository, StopTimeRepository stopTimeRepository) {
        this.agencyRepository = agencyRepository;
        this.calendarRepository = calendarRepository;
        this.fareAttributeRepository = fareAttributeRepository;
        this.fareRuleRepository = fareRuleRepository;
        this.routeRepository = routeRepository;
        this.tripRepository = tripRepository;
        this.stopRepository = stopRepository;
        this.stopTimeRepository = stopTimeRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        if (agencyRepository.count() > 0) {
            logger.info("Data already loaded. Skipping CSV import.");
            return;
        }

        logger.info("Database is empty. Starting to load data from all CSV files...");

        loadDataForPattern("classpath:static/agency.csv", Agency.class, agencyRepository);
        loadDataForPattern("classpath:static/calendar.csv", Calendar.class, calendarRepository);
        loadDataForPattern("classpath:static/stops.csv", Stop.class, stopRepository);
        loadDataForPattern("classpath:static/routes.csv", Route.class, routeRepository);
        loadDataForPattern("classpath:static/trips.csv", Trip.class, tripRepository);

        loadDataForPattern("classpath:static/fare_attributes_part_*.csv", FareAttribute.class, fareAttributeRepository);
        loadDataForPattern("classpath:static/fare_rules_part_*.csv", FareRule.class, fareRuleRepository);
        loadDataForPattern("classpath:static/stop_times_part_*.csv", StopTime.class, stopTimeRepository);

        logger.info("All GTFS data has been successfully loaded into the database.");
    }

    private <T, ID> void loadDataForPattern(String pattern, Class<T> type, JpaRepository<T, ID> repository) {
        logger.info("Preparing to load data for pattern: {}", pattern);
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources(pattern);

            if (resources.length == 0) {
                logger.warn("No resources found for pattern: {}", pattern);
                return;
            }

            for (Resource resource : resources) {
                logger.info("Loading data from: {}", resource.getFilename());
                try (Reader reader = new InputStreamReader(resource.getInputStream())) {
                    List<T> records = new CsvToBeanBuilder<T>(reader)
                            .withType(type)
                            .withIgnoreLeadingWhiteSpace(true)
                            .build()
                            .parse();

                    if (ENTITYId_GETTERS.containsKey(type)) {
                        Function<Object, String> idGetter = ENTITYId_GETTERS.get(type);
                        int originalSize = records.size();
                        records.removeIf(record -> {
                            String id = idGetter.apply(record);
                            return id == null || id.isBlank();
                        });
                        int removedCount = originalSize - records.size();
                        if (removedCount > 0) {
                            logger.warn("Removed {} invalid records (with null or blank IDs) from {}", removedCount, resource.getFilename());
                        }
                    }

                    if (!records.isEmpty()) {
                        repository.saveAll(records);
                        logger.info("Successfully loaded and saved {} valid records from {}", records.size(), resource.getFilename());
                    } else {
                        logger.warn("No valid records found in file: {}", resource.getFilename());
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error loading data for pattern {}: {}", pattern, e.getMessage());
            throw new RuntimeException("Failed to load CSV data for pattern: " + pattern, e);
        }
    }
}

