package com.spring.Live.Vehicle.Map.Delhi.repository;

import com.spring.Live.Vehicle.Map.Delhi.model.Stop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StopRepository extends JpaRepository<Stop, String> {
    Optional<Stop> findByStopName(String stopName);

}
