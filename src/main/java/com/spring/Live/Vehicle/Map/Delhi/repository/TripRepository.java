package com.spring.Live.Vehicle.Map.Delhi.repository;

import com.spring.Live.Vehicle.Map.Delhi.model.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TripRepository extends JpaRepository<Trip, String> {
    // In TripRepository.java

}