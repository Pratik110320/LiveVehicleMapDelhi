package com.spring.Live.Vehicle.Map.Delhi.service;

import com.spring.Live.Vehicle.Map.Delhi.model.*;
import com.spring.Live.Vehicle.Map.Delhi.model.Calendar;
import com.spring.Live.Vehicle.Map.Delhi.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GtfsStaticService {

    @Autowired
    private StopRepository stopRepository;

    @Autowired
    private StopTimeRepository stopTimeRepository;

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private RouteRepository routeRepository;

    @Autowired
    private CalendarRepository calendarRepository;

    @Cacheable("stops")
    public List<Stop> getAllStops() {
        return stopRepository.findAll();
    }

    @Cacheable("routes")
    public List<Route> getAllRoutes() {
        return routeRepository.findAll();
    }

    @Cacheable(value = "schedules", key = "#stopId")
    public List<Map<String, String>> getScheduleForStop(String stopId) {
        // Step 1: Find which services are active today
        String todayYYYYMMDD = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        DayOfWeek todayDayOfWeek = LocalDate.now().getDayOfWeek();

        List<Calendar> activeCalendars = calendarRepository
                .findActiveServicesForDate(todayYYYYMMDD).stream()
                .filter(cal -> isServiceActiveToday(cal, todayDayOfWeek))
                .collect(Collectors.toList());

        Set<String> activeServiceIds = activeCalendars.stream()
                .map(Calendar::getServiceId)
                .collect(Collectors.toSet());

        if (activeServiceIds.isEmpty()) {
            return Collections.emptyList(); // No services running today
        }

        // Step 2: Use the optimized query to get schedules for active services
        List<ScheduleProjection> schedules = stopTimeRepository.findScheduleByStopIdAndServiceIdIn(stopId, activeServiceIds);

        // Step 3: Filter for future times and format the output
        LocalTime now = LocalTime.now(ZoneId.of("Asia/Kolkata"));
        return schedules.stream()
                .map(s -> Map.of(
                        "routeName", s.getRouteShortName(),
                        "arrivalTime", s.getArrivalTime()
                ))
                .filter(entry -> {
                    try {
                        LocalTime arrivalTime = LocalTime.parse(entry.get("arrivalTime"), DateTimeFormatter.ofPattern("HH:mm:ss"));
                        return arrivalTime.isAfter(now);
                    } catch (DateTimeParseException e) {
                        return false; // Exclude if parsing fails
                    }
                })
                .sorted(Comparator.comparing(e -> e.get("arrivalTime")))
                .limit(10) // Only show the next 10 upcoming buses
                .collect(Collectors.toList());
    }

    private boolean isServiceActiveToday(Calendar cal, DayOfWeek day) {
        return switch (day) {
            case MONDAY -> cal.isMonday();
            case TUESDAY -> cal.isTuesday();
            case WEDNESDAY -> cal.isWednesday();
            case THURSDAY -> cal.isThursday();
            case FRIDAY -> cal.isFriday();
            case SATURDAY -> cal.isSaturday();
            case SUNDAY -> cal.isSunday();
        };
    }
}
