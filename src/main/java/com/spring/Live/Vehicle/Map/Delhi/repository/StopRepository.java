package com.spring.Live.Vehicle.Map.Delhi.repository;

import com.spring.Live.Vehicle.Map.Delhi.model.Stop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StopRepository extends JpaRepository<Stop, String> {
    @Query("SELECT s FROM Stop s WHERE " +
            "s.stopName LIKE %?1% OR s.stopCode LIKE %?1%")
    List<Stop> searchByNameOrCode(String keyword);
}
