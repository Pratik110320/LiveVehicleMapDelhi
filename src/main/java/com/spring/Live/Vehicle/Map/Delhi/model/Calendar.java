package com.spring.Live.Vehicle.Map.Delhi.model;

import com.opencsv.bean.CsvBindByName;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;


@Entity
@Table(name = "calendar")
public class Calendar {

    @Id
    @CsvBindByName(column = "service_id")
    @Column(name = "service_id")
    private String serviceId;

    @CsvBindByName(column = "monday")
    private int monday;

    @CsvBindByName(column = "tuesday")
    private int tuesday;

    @CsvBindByName(column = "wednesday")
    private int wednesday;

    @CsvBindByName(column = "thursday")
    private int thursday;

    @CsvBindByName(column = "friday")
    private int friday;

    @CsvBindByName(column = "saturday")
    private int saturday;

    @CsvBindByName(column = "sunday")
    private int sunday;

    @CsvBindByName(column = "start_date")
    private String startDate;

    @CsvBindByName(column = "end_date")
    private String endDate;

    public Calendar() {
    }

    public boolean isMonday() {
        return monday == 1;
    }

    public boolean isTuesday() {
        return tuesday == 1;
    }

    public boolean isWednesday() {
        return wednesday == 1;
    }

    public boolean isThursday() {
        return thursday == 1;
    }

    public boolean isFriday() {
        return friday == 1;
    }

    public boolean isSaturday() {
        return saturday == 1;
    }

    public boolean isSunday() {
        return sunday == 1;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public int getMonday() {
        return monday;
    }

    public void setMonday(int monday) {
        this.monday = monday;
    }

    public int getTuesday() {
        return tuesday;
    }

    public void setTuesday(int tuesday) {
        this.tuesday = tuesday;
    }

    public int getWednesday() {
        return wednesday;
    }

    public void setWednesday(int wednesday) {
        this.wednesday = wednesday;
    }

    public int getThursday() {
        return thursday;
    }

    public void setThursday(int thursday) {
        this.thursday = thursday;
    }

    public int getFriday() {
        return friday;
    }

    public void setFriday(int friday) {
        this.friday = friday;
    }

    public int getSaturday() {
        return saturday;
    }

    public void setSaturday(int saturday) {
        this.saturday = saturday;
    }

    public int getSunday() {
        return sunday;
    }

    public void setSunday(int sunday) {
        this.sunday = sunday;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }
}
