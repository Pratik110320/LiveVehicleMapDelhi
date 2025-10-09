package com.spring.Live.Vehicle.Map.Delhi.model;

import com.opencsv.bean.CsvBindByName;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "fare_attributes")
public class FareAttribute {

    @Id
    @CsvBindByName(column = "fare_id")
    @Column(name = "fare_id")
    private String fareId;

    @CsvBindByName(column = "price")
    private float price;

    @CsvBindByName(column = "currency_type")
    private String currencyType;

    @CsvBindByName(column = "payment_method")
    private int paymentMethod;

    @CsvBindByName(column = "transfers")
    private int transfers;

    @CsvBindByName(column = "agency_id")
    private String agencyId;

    public FareAttribute() {
    }

    public String getFareId() {
        return fareId;
    }

    public void setFareId(String fareId) {
        this.fareId = fareId;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public String getCurrencyType() {
        return currencyType;
    }

    public void setCurrencyType(String currencyType) {
        this.currencyType = currencyType;
    }

    public int getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(int paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public int getTransfers() {
        return transfers;
    }

    public void setTransfers(int transfers) {
        this.transfers = transfers;
    }

    public String getAgencyId() {
        return agencyId;
    }

    public void setAgencyId(String agencyId) {
        this.agencyId = agencyId;
    }
}
