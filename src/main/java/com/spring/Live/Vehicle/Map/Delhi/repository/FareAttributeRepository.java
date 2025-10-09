package com.spring.Live.Vehicle.Map.Delhi.repository;

import com.spring.Live.Vehicle.Map.Delhi.model.FareAttribute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FareAttributeRepository extends JpaRepository<FareAttribute, String> {
    List<FareAttribute> findByAgencyId(String agencyId);

    @Query("SELECT fa FROM FareAttribute fa WHERE fa.price <= ?1")
    List<FareAttribute> findByMaxPrice(float maxPrice);
}
