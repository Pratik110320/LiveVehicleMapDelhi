package com.spring.Live.Vehicle.Map.Delhi.repository;

import com.spring.Live.Vehicle.Map.Delhi.model.StopTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StopTimeRepository extends JpaRepository<StopTime, String> {

    List<StopTime> findByStopId(String stopId);
    List<StopTime> findByTripIdOrderByStopSequence(String tripId);


}
