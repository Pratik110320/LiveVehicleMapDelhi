package com.spring.Live.Vehicle.Map.Delhi.model;

import com.opencsv.bean.CsvBindByName;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class FareAttributeDto {
    @CsvBindByName(column = "fare_id")
    private String fareId;
    @CsvBindByName
    private BigDecimal price;
    @CsvBindByName(column = "currency_type")
    private String currencyType;
    @CsvBindByName(column = "payment_method")
    private int paymentMethod;
    @CsvBindByName
    private int transfers;
    @CsvBindByName(column = "agency_id")
    private String agencyId;

    @CsvBindByName(column = "old_fare_id")
    private String oldFareId;

    private List<FareRuleDto> fareRules = new ArrayList<>();

    public FareAttributeDto() {}
    // Getters and Setters
    public String getFareId() { return fareId; }
    public void setFareId(String fareId) { this.fareId = fareId; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public String getCurrencyType() { return currencyType; }
    public void setCurrencyType(String currencyType) { this.currencyType = currencyType; }
    public int getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(int paymentMethod) { this.paymentMethod = paymentMethod; }
    public int getTransfers() { return transfers; }
    public void setTransfers(int transfers) { this.transfers = transfers; }

    public String getAgencyId() { return agencyId; }
    public void setAgencyId(String agencyId) { this.agencyId = agencyId; }

    public String getOldFareId() { return oldFareId; }
    public void setOldFareId(String oldFareId) { this.oldFareId = oldFareId; }

    public List<FareRuleDto> getFareRules() { return fareRules; }
    public void setFareRules(List<FareRuleDto> fareRules) { this.fareRules = fareRules; }
    public void addFareRule(FareRuleDto rule) {
        if (this.fareRules == null) this.fareRules = new ArrayList<>();
        this.fareRules.add(rule);
    }
}
