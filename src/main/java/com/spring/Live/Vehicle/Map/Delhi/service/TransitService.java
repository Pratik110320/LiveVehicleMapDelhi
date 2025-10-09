package com.spring.Live.Vehicle.Map.Delhi.service;


import com.spring.Live.Vehicle.Map.Delhi.model.*;
import com.spring.Live.Vehicle.Map.Delhi.repository.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransitService {

    private final AgencyRepository agencyRepository;
    private final RouteRepository routeRepository;
    private final TripRepository tripRepository;
    private final StopTimeRepository stopTimeRepository;
    private final StopRepository stopRepository;

    public TransitService(AgencyRepository agencyRepository, RouteRepository routeRepository, TripRepository tripRepository, StopTimeRepository stopTimeRepository, StopRepository stopRepository) {
        this.agencyRepository = agencyRepository;
        this.routeRepository = routeRepository;
        this.tripRepository = tripRepository;
        this.stopTimeRepository = stopTimeRepository;
        this.stopRepository = stopRepository;
    }

    public List<Agency> getAllAgencies() {
        return agencyRepository.findAll();
    }

    public List<Route> getAllRoutes() {
        return routeRepository.findAll();
    }

    public List<Trip> getAllTrips() {
        return tripRepository.findAll();
    }

    public List<Trip> getTripsByRouteId(String routeId) {
        return tripRepository.findByRouteId(routeId);
    }

    public List<Stop> getStopsForTrip(String tripId) {
        List<StopTime> tripStopTimes = stopTimeRepository.findByTripIdOrderByStopSequenceAsc(tripId);
        List<String> stopIds = tripStopTimes.stream()
                .map(StopTime::getStopId)
                .collect(Collectors.toList());
        List<Stop> stops = stopRepository.findAllById(stopIds);
        return stops;
    }
}

