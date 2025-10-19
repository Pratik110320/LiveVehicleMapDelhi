package com.spring.Live.Vehicle.Map.Delhi.repository;

import com.spring.Live.Vehicle.Map.Delhi.model.Stop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface StopRepository extends JpaRepository<Stop, String> {
    // Corrected to be case-insensitive for better usability
    @Query("SELECT s FROM Stop s WHERE LOWER(s.stopName) = LOWER(:stopName)")
    Optional<Stop> findByStopName(@Param("stopName") String stopName);
}
