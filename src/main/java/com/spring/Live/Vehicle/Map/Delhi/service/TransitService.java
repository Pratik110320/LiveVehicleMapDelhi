package com.spring.Live.Vehicle.Map.Delhi.service;

import com.spring.Live.Vehicle.Map.Delhi.model.Trip;
import com.spring.Live.Vehicle.Map.Delhi.repository.ScheduleProjection;
import com.spring.Live.Vehicle.Map.Delhi.repository.StopTimeRepository;
import com.spring.Live.Vehicle.Map.Delhi.repository.TripRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TransitService {

    private final TripRepository tripRepository;
    private final StopTimeRepository stopTimeRepository;

    public TransitService(TripRepository tripRepository, StopTimeRepository stopTimeRepository) {
        this.tripRepository = tripRepository;
        this.stopTimeRepository = stopTimeRepository;
    }

    public List<Trip> findTripsByRouteAndDirection(String routeId, int directionId) {
        return tripRepository.findByRouteIdAndDirectionId(routeId, directionId);
    }

    public List<ScheduleProjection> getScheduleForStop(String stopId) {
        return stopTimeRepository.findScheduleByStopId(stopId);
    }
}
