package com.spring.Live.Vehicle.Map.Delhi.repository;

import com.spring.Live.Vehicle.Map.Delhi.model.StopTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StopTimeRepository extends JpaRepository<StopTime, Long> {

    List<StopTime> findByTripId(String tripId);

    List<StopTime> findByStopId(String stopId);

    @Query("SELECT st FROM StopTime st WHERE st.tripId = ?1 AND st.stopId = ?2")
    List<StopTime> findByTripIdAndStopId(String tripId, String stopId);

    @Query("SELECT st FROM StopTime st WHERE st.tripId = ?1 ORDER BY st.stopSequence ASC")
    List<StopTime> findByTripIdOrderByStopSequence(String tripId);

    @Query("SELECT st FROM StopTime st WHERE st.stopId = ?1 " +
            "ORDER BY st.departureTime ASC")
    List<StopTime> findByStopIdOrderByDepartureTime(String stopId);
}
