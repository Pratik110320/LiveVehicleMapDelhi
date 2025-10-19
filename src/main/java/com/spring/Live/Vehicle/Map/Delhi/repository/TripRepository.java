package com.spring.Live.Vehicle.Map.Delhi.repository;

import com.spring.Live.Vehicle.Map.Delhi.model.Route;
import com.spring.Live.Vehicle.Map.Delhi.model.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TripRepository extends JpaRepository<Trip, String> {
    @Query("SELECT r FROM Route r JOIN Trip t ON r.routeId = t.routeId JOIN StopTime st ON t.tripId = st.tripId JOIN Stop s ON st.stopId = s.stopId WHERE LOWER(s.stopName) = LOWER(:stopName)")
    List<Route> findRoutesByStopName(@Param("stopName") String stopName);
}