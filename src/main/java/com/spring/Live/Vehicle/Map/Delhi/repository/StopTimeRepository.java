package com.spring.Live.Vehicle.Map.Delhi.repository;


import com.spring.Live.Vehicle.Map.Delhi.model.SidebarDepartureDTO;
import com.spring.Live.Vehicle.Map.Delhi.model.StopBusScheduleDTO;
import com.spring.Live.Vehicle.Map.Delhi.model.StopTime;
import com.spring.Live.Vehicle.Map.Delhi.model.StopTimeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StopTimeRepository extends JpaRepository<StopTime, StopTimeId> {
    List<StopTime> findByStopId(String stopId);
    List<StopTime> findByTripId(String tripId);


    @Query("SELECT NEW com.spring.Live.Vehicle.Map.Delhi.model.StopBusScheduleDTO(" +
            "r.routeId, r.routeShortName, r.routeLongName, st.arrivalTime, st.departureTime) " +
            "FROM StopTime st " +
            "JOIN Trip t ON st.tripId = t.tripId " +
            "JOIN Route r ON t.routeId = r.routeId " +
            "JOIN Calendar c ON t.serviceId = c.serviceId " +
            "WHERE st.stopId = :stopId " +
            "AND c.startDate <= :todayStr AND c.endDate >= :todayStr " +
            "AND ( CASE :dayOfWeek " +
            "    WHEN 'monday' THEN c.monday " +
            "    WHEN 'tuesday' THEN c.tuesday " +
            "    WHEN 'wednesday' THEN c.wednesday " +
            "    WHEN 'thursday' THEN c.thursday " +
            "    WHEN 'friday' THEN c.friday " +
            "    WHEN 'saturday' THEN c.saturday " +
            "    WHEN 'sunday' THEN c.sunday " +
            "    ELSE 0 END = 1 ) " +
            "ORDER BY st.arrivalTime") // Added an order by for good measure
    List<StopBusScheduleDTO> findStopSchedulesForToday(
            @Param("stopId") String stopId,
            @Param("todayStr") String todayStr,
            @Param("dayOfWeek") String dayOfWeek
    );


    @Query("SELECT NEW com.spring.Live.Vehicle.Map.Delhi.model.SidebarDepartureDTO(" +
            "s.stopId, s.stopName, r.routeId, r.routeShortName, r.routeLongName, st.arrivalTime) " +
            "FROM StopTime st " +
            "JOIN Stop s ON st.stopId = s.stopId " + // Join Stop to get name
            "JOIN Trip t ON st.tripId = t.tripId " +
            "JOIN Route r ON t.routeId = r.routeId " +
            "JOIN Calendar c ON t.serviceId = c.serviceId " +
            "WHERE st.stopId IN (:stopIds) " + // Filter by list of stop IDs
            "AND c.startDate <= :todayStr AND c.endDate >= :todayStr " +
            "AND ( CASE :dayOfWeek " +
            "    WHEN 'monday' THEN c.monday " +
            "    WHEN 'tuesday' THEN c.tuesday " +
            "    WHEN 'wednesday' THEN c.wednesday " +
            "    WHEN 'thursday' THEN c.thursday " +
            "    WHEN 'friday' THEN c.friday " +
            "    WHEN 'saturday' THEN c.saturday " +
            "    WHEN 'sunday' THEN c.sunday " +
            "    ELSE 0 END = 1 )")
    List<SidebarDepartureDTO> findSidebarDepartures(
            @Param("stopIds") List<String> stopIds,
            @Param("todayStr") String todayStr,
            @Param("dayOfWeek") String dayOfWeek
    );
}

