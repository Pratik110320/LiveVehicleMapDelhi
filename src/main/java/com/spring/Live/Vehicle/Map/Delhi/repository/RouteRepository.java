package com.spring.Live.Vehicle.Map.Delhi.repository;

import com.spring.Live.Vehicle.Map.Delhi.model.Route;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RouteRepository extends JpaRepository<Route, String> {

    List<Route> findByRouteShortNameContainingOrRouteLongNameContaining(String shortName, String longName);

}
