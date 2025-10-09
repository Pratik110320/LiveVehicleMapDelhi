package com.spring.Live.Vehicle.Map.Delhi.repository;

import com.spring.Live.Vehicle.Map.Delhi.model.Calendar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CalendarRepository extends JpaRepository<Calendar, String> {
    @Query("SELECT c FROM Calendar c WHERE " +
            "c.startDate <= ?1 AND c.endDate >= ?1")
    List<Calendar> findActiveServicesForDate(String date);
}
