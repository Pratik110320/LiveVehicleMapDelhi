package com.spring.Live.Vehicle.Map.Delhi.repository;

import com.spring.Live.Vehicle.Map.Delhi.model.FareRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FareRuleRepository extends JpaRepository<FareRule, Long> {
    List<FareRule> findByRouteId(String routeId);

    List<FareRule> findByFareId(String fareId);

    @Query("SELECT fr FROM FareRule fr WHERE " +
            "fr.routeId = ?1 AND fr.originId = ?2 AND fr.destinationId = ?3")
    List<FareRule> findByRouteAndOriginDestination(
            String routeId, String originId, String destinationId);
}

