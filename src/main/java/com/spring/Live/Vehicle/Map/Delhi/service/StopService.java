package com.spring.Live.Vehicle.Map.Delhi.service;

import com.spring.Live.Vehicle.Map.Delhi.model.CsvDataLoader;
import com.spring.Live.Vehicle.Map.Delhi.model.StopDto;
import com.spring.Live.Vehicle.Map.Delhi.model.StopTimeDto;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StopService {
    private final CsvDataLoader loader;
    private final DateTimeFormatter tf = DateTimeFormatter.ofPattern("H:mm:ss");

    public StopService(CsvDataLoader loader) {
        this.loader = loader;
    }

    public StopDto getStop(String stopId) {
        return loader.getStopById(stopId);
    }

    // arrivals between fromTime and toTime (both optional)
    public List<StopTimeDto> getArrivals(String stopId, String fromTimeStr, String toTimeStr) {
        List<StopTimeDto> stopTimes = loader.getStopTimesForStop(stopId);
        if ((fromTimeStr == null || fromTimeStr.isEmpty()) && (toTimeStr == null || toTimeStr.isEmpty())) {
            return stopTimes.stream()
                    .sorted(Comparator.comparingInt(StopTimeDto::getStopSequence)) // or by arrival_time parsed
                    .collect(Collectors.toList());
        }
        LocalTime from = fromTimeStr == null ? LocalTime.MIN : LocalTime.parse(fromTimeStr, tf);
        LocalTime to = toTimeStr == null ? LocalTime.MAX : LocalTime.parse(toTimeStr, tf);

        return stopTimes.stream()
                .filter(st -> {
                    try {
                        LocalTime arr = LocalTime.parse(st.getArrivalTime(), tf);
                        return (!arr.isBefore(from) && !arr.isAfter(to));
                    } catch (Exception e) {
                        return false;
                    }
                })
                .sorted(Comparator.comparing(st -> LocalTime.parse(st.getArrivalTime(), tf)))
                .collect(Collectors.toList());
    }
}
