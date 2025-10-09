package com.spring.Live.Vehicle.Map.Delhi.service;

import com.spring.Live.Vehicle.Map.Delhi.model.*;
import com.spring.Live.Vehicle.Map.Delhi.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Service class for accessing static GTFS data from the database.
 * This service provides read-only access to the foundational transit data
 * like routes, stops, and trips, which rarely changes.
 */
@Service
@Transactional(readOnly = true) // Default to read-only transactions for performance
public class GtfsStaticService {

    private static final Logger logger = LoggerFactory.getLogger(GtfsStaticService.class);

    private final RouteRepository routeRepository;
    private final StopRepository stopRepository;
    private final TripRepository tripRepository;
    private final AgencyRepository agencyRepository;
    private final CalendarRepository calendarRepository;

    @Autowired
    public GtfsStaticService(RouteRepository routeRepository, StopRepository stopRepository, TripRepository tripRepository, AgencyRepository agencyRepository, CalendarRepository calendarRepository) {
        this.routeRepository = routeRepository;
        this.stopRepository = stopRepository;
        this.tripRepository = tripRepository;
        this.agencyRepository = agencyRepository;
        this.calendarRepository = calendarRepository;
    }

    // --- Route Methods ---
    public List<Route> getAllRoutes() {
        try {
            return routeRepository.findAll();
        } catch (Exception e) {
            logger.error("Error fetching all routes", e);
            return Collections.emptyList();
        }
    }

    public Optional<Route> getRouteById(String routeId) {
        try {
            return routeRepository.findById(routeId);
        } catch (Exception e) {
            logger.error("Error fetching route with ID: " + routeId, e);
            return Optional.empty();
        }
    }

    // --- Stop Methods ---
    public List<Stop> getAllStops() {
        try {
            return stopRepository.findAll();
        } catch (Exception e) {
            logger.error("Error fetching all stops", e);
            return Collections.emptyList();
        }
    }

    public Optional<Stop> getStopById(String stopId) {
        try {
            return stopRepository.findById(stopId);
        } catch (Exception e) {
            logger.error("Error fetching stop with ID: " + stopId, e);
            return Optional.empty();
        }
    }

    // --- Trip Methods ---
    public List<Trip> getAllTrips() {
        try {
            return tripRepository.findAll();
        } catch (Exception e) {
            logger.error("Error fetching all trips", e);
            return Collections.emptyList();
        }
    }

    // --- Agency Methods ---
    public List<Agency> getAllAgencies() {
        try {
            return agencyRepository.findAll();
        } catch (Exception e) {
            logger.error("Error fetching all agencies", e);
            return Collections.emptyList();
        }
    }

    // --- Calendar Methods ---
    public List<Calendar> getAllCalendars() {
        try {
            return calendarRepository.findAll();
        } catch (Exception e) {
            logger.error("Error fetching all calendars", e);
            return Collections.emptyList();
        }
    }
}

