package com.spring.Live.Vehicle.Map.Delhi.service;

import com.spring.Live.Vehicle.Map.Delhi.model.CsvDataLoader;
import com.spring.Live.Vehicle.Map.Delhi.model.StopTimeDto;
import com.spring.Live.Vehicle.Map.Delhi.model.TripDto;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TripService {
    private final CsvDataLoader loader;

    public TripService(CsvDataLoader loader) {
        this.loader = loader;
    }

    public TripDto getTrip(String tripId) {
        return loader.getTripById(tripId);
    }

    public List<StopTimeDto> getStopTimesForTrip(String tripId) {
        List<StopTimeDto> list = loader.getStopTimesForTrip(tripId);

        // Return in-order by stop_sequence
        return list.stream()
                .sorted(Comparator.comparingInt(StopTimeDto::getStopSequence))
                .collect(Collectors.toList());
    }
}
