package com.spring.Live.Vehicle.Map.Delhi.controller;


import com.spring.Live.Vehicle.Map.Delhi.model.*;
import com.spring.Live.Vehicle.Map.Delhi.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/stops")
public class StopController {

    @Autowired
    private StopRepository stopRepository;

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private RouteRepository routeRepository;

    @Autowired
    private StopTimeRepository stopTimeRepository;


    @Autowired
    private CalendarRepository calendarRepository;

    @GetMapping("/all")
    public List<Stop> getAllStops() {
        return stopRepository.findAll();
    }

    @GetMapping("/visible")
    public List<Stop> getVisibleStops(
            @RequestParam double minLat,
            @RequestParam double maxLat,
            @RequestParam double minLon,
            @RequestParam double maxLon) {
        return stopRepository.findByStopLatBetweenAndStopLonBetween(minLat, maxLat, minLon, maxLon);
    }

    @GetMapping("/{stopId}/buses")
    public List<StopTime> getBusesAtStop(@PathVariable String stopId) {
        List<StopTime> result = stopTimeRepository.findByStopId(stopId);
        return result != null ? result : new ArrayList<>();
    }


    @GetMapping("/search")
    public List<Stop> searchStops(@RequestParam String Name) {
        return stopRepository.findByStopNameContainingIgnoreCase(Name);
    }

    @GetMapping("/{stopId}/routesToday")
    public List<StopBusScheduleDTO> getRoutesAtStopToday(@PathVariable String stopId) {
        LocalDate today = LocalDate.now();
        String todayStr = today.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String dayOfWeek = today.getDayOfWeek().toString().toLowerCase();  // "wednesday"

        List<StopTime> stopTimes = stopTimeRepository.findByStopId(stopId);
        List<StopBusScheduleDTO> result = new ArrayList<>();

        for (StopTime st : stopTimes) {
            Trip trip = tripRepository.findById(st.getTripId()).orElse(null);
            if (trip == null) continue;
            Calendar calendar = calendarRepository.findById(trip.getServiceId()).orElse(null);
            if (calendar == null) continue;
            if (todayStr.compareTo(calendar.getStartDate()) < 0 ||
                    todayStr.compareTo(calendar.getEndDate()) > 0) continue;

            // LOG calendar week values
            System.out.println("Today is: " + dayOfWeek +
                    "; Calendar: mon=" + calendar.getMonday() +
                    " tue=" + calendar.getTuesday() +
                    " wed=" + calendar.getWednesday() +
                    " thu=" + calendar.getThursday() +
                    " fri=" + calendar.getFriday() +
                    " sat=" + calendar.getSaturday() +
                    " sun=" + calendar.getSunday());

            boolean runsToday = false;
            switch(dayOfWeek) {
                case "monday": runsToday = (calendar.getMonday() == 1); break;
                case "tuesday": runsToday = (calendar.getTuesday() == 1); break;
                case "wednesday": runsToday = (calendar.getWednesday() == 1); break;
                case "thursday": runsToday = (calendar.getThursday() == 1); break;
                case "friday": runsToday = (calendar.getFriday() == 1); break;
                case "saturday": runsToday = (calendar.getSaturday() == 1); break;
                case "sunday": runsToday = (calendar.getSunday() == 1); break;
            }
            System.out.println("runsToday: " + runsToday);

            if (!runsToday) continue;

            Route route = routeRepository.findById(trip.getRouteId()).orElse(null);
            if (route == null) continue;

            StopBusScheduleDTO dto = new StopBusScheduleDTO();
            dto.setRouteId(route.getRouteId());
            dto.setRouteShortName(route.getRouteShortName());
            dto.setRouteLongName(route.getRouteLongName());
            dto.setArrivalTime(st.getArrivalTime());
            dto.setDepartureTime(st.getDepartureTime());
            result.add(dto);
        }
        System.out.println("Returning " + result.size() + " bus routes for stop " + stopId);
        return result;
    }

}
