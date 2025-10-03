package com.spring.Live.Vehicle.Map.Delhi.model;

import com.opencsv.bean.CsvBindByName;
import java.math.BigDecimal;

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
}
