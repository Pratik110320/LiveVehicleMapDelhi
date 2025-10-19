package com.spring.Live.Vehicle.Map.Delhi.repository;

import com.spring.Live.Vehicle.Map.Delhi.model.StopTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface StopTimeRepository extends JpaRepository<StopTime, String> {

    List<StopTime> findByStopId(String stopId);

    // This method was added back to resolve a compilation error.
    List<StopTime> findByTripIdOrderByStopSequence(String tripId);

    // Optimized query to fetch schedule data by joining tables, preventing the N+1 problem.
    // It also filters by currently active service IDs.
    @Query("SELECT st.arrivalTime as arrivalTime, r.routeShortName as routeShortName " +
            "FROM StopTime st " +
            "JOIN Trip t ON st.tripId = t.tripId " +
            "JOIN Route r ON t.routeId = r.routeId " +
            "WHERE st.stopId = :stopId AND t.serviceId IN :serviceIds")
    List<ScheduleProjection> findScheduleByStopIdAndServiceIdIn(@Param("stopId") String stopId, @Param("serviceIds") Set<String> serviceIds);
}

