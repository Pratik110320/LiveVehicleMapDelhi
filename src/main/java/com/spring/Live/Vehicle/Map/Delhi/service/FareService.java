package com.spring.Live.Vehicle.Map.Delhi.service;

import com.spring.Live.Vehicle.Map.Delhi.model.CsvDataLoader;
import com.spring.Live.Vehicle.Map.Delhi.model.FareAttributeDto;
import com.spring.Live.Vehicle.Map.Delhi.model.FareRuleDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FareService {
    @Autowired
    private final CsvDataLoader loader;

    public FareService(CsvDataLoader loader) {
        this.loader = loader;
    }

    public List<FareAttributeDto> getFaresForRoute(String routeId) {
        List<FareRuleDto> rules = loader.getFareRulesForRoute(routeId);
        return rules.stream()
                .map(FareRuleDto::getFareAttribute)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }

    // simple match: find fare rule where origin/destination exactly match
    public Optional<FareAttributeDto> getFareForOriginDest(String originId, String destId,String routeId) {
        return loader.getFareRulesForRoute(routeId).stream()
                .filter(fr -> originId.equals(fr.getOriginId()) && destId.equals(fr.getDestinationId()))
                .map(FareRuleDto::getFareAttribute)
                .filter(Objects::nonNull)
                .findFirst();
    }
}
