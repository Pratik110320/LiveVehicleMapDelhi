package com.spring.Live.Vehicle.Map.Delhi.repository;

import com.spring.Live.Vehicle.Map.Delhi.model.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TripRepository extends JpaRepository<Trip, String> {
    List<Trip> findByRouteId(String routeId);

    List<Trip> findByServiceId(String serviceId);

    @Query("SELECT t FROM Trip t WHERE t.routeId = ?1 AND t.serviceId = ?2")
    List<Trip> findByRouteIdAndServiceId(String routeId, String serviceId);}