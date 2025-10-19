package com.spring.Live.Vehicle.Map.Delhi.service;

import com.spring.Live.Vehicle.Map.Delhi.model.Stop;
import com.spring.Live.Vehicle.Map.Delhi.model.StopTime;
import com.spring.Live.Vehicle.Map.Delhi.repository.StopRepository;
import com.spring.Live.Vehicle.Map.Delhi.repository.StopTimeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class TransitService {

    @Autowired
    private StopTimeRepository stopTimeRepository;

    @Autowired
    private StopRepository stopRepository;

    public List<Stop> findRoute(Stop from, Stop to) {
        // Find all stop times (which correspond to trips) for the starting and ending stops.
        List<StopTime> fromStopTimes = stopTimeRepository.findByStopId(from.getStopId());
        List<StopTime> toStopTimes = stopTimeRepository.findByStopId(to.getStopId());

        // Extract the trip IDs for each set of stop times.
        Set<String> fromTripIds = fromStopTimes.stream().map(StopTime::getTripId).collect(Collectors.toSet());
        Set<String> toTripIds = toStopTimes.stream().map(StopTime::getTripId).collect(Collectors.toSet());

        // Find the intersection of the two sets to get trips that visit both stops.
        fromTripIds.retainAll(toTripIds);

        if (fromTripIds.isEmpty()) {
            // No direct route found.
            return Collections.emptyList();
        }

        // Pick the first common trip ID to build the route.
        String commonTripId = fromTripIds.iterator().next();

        // Get all stop times for this common trip, ordered by sequence.
        List<StopTime> tripStopTimes = stopTimeRepository.findByTripIdOrderByStopSequence(commonTripId);

        // Find the sequence numbers for our start and end stops within this trip.
        int fromSequence = -1;
        int toSequence = -1;

        for (StopTime st : tripStopTimes) {
            if (st.getStopId().equals(from.getStopId())) {
                fromSequence = st.getStopSequence();
            }
            if (st.getStopId().equals(to.getStopId())) {
                toSequence = st.getStopSequence();
            }
        }

        // Ensure the "from" stop comes before the "to" stop in the sequence.
        if (fromSequence == -1 || toSequence == -1 || fromSequence >= toSequence) {
            return Collections.emptyList();
        }

        // Filter the stops to get only those between our start and end points.
        List<String> routeStopIds = new ArrayList<>();
        for (StopTime st : tripStopTimes) {
            if (st.getStopSequence() >= fromSequence && st.getStopSequence() <= toSequence) {
                routeStopIds.add(st.getStopId());
            }
        }

        // Fetch the full Stop objects for the route.
        List<Stop> stopsOnRoute = stopRepository.findAllById(routeStopIds);

        // Sort the final list of stops according to the trip sequence.
        stopsOnRoute.sort(Comparator.comparingInt(stop -> routeStopIds.indexOf(stop.getStopId())));

        return stopsOnRoute;
    }
}
