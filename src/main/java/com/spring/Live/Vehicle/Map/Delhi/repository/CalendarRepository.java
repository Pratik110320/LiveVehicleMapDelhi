package com.spring.Live.Vehicle.Map.Delhi.repository;

import com.spring.Live.Vehicle.Map.Delhi.model.Calendar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CalendarRepository extends JpaRepository<Calendar, String> {

    // Query to find all services that are active for a given date.
    @Query("SELECT c FROM Calendar c WHERE c.startDate <= :date AND c.endDate >= :date")
    List<Calendar> findActiveServicesForDate(@Param("date") String date);
}
