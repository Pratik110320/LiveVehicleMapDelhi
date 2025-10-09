package com.spring.Live.Vehicle.Map.Delhi.repository;

import com.spring.Live.Vehicle.Map.Delhi.model.Route;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RouteRepository extends JpaRepository<Route, String> {

    List<Route> findByAgencyId(String agencyId);

    @Query("SELECT r FROM Route r WHERE " +
            "r.routeShortName LIKE %?1% OR r.routeLongName LIKE %?1%")
    List<Route> searchByName(String keyword);
}
