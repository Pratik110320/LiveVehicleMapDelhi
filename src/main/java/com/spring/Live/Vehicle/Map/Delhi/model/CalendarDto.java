package com.spring.Live.Vehicle.Map.Delhi.model;

import com.opencsv.bean.CsvBindByName;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class CalendarDto {
    private static final DateTimeFormatter GTFS_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");


    @CsvBindByName(column = "service_id")
    private String serviceId;
    @CsvBindByName
    private boolean monday;
    @CsvBindByName
    private boolean tuesday;
    @CsvBindByName
    private boolean wednesday;
    @CsvBindByName
    private boolean thursday;
    @CsvBindByName
    private boolean friday;
    @CsvBindByName
    private boolean saturday;
    @CsvBindByName
    private boolean sunday;
    @CsvBindByName(column = "start_date")
    private LocalDate startDate;
    @CsvBindByName(column = "end_date")
    private LocalDate endDate;


    public boolean isActiveOn(LocalDate date) {
        if (date.isBefore(startDate) || date.isAfter(endDate)) {
            return false;
        }
        return switch (date.getDayOfWeek()) {
            case MONDAY -> monday;
            case TUESDAY -> tuesday;
            case WEDNESDAY -> wednesday;
            case THURSDAY -> thursday;
            case FRIDAY -> friday;
            case SATURDAY -> saturday;
            case SUNDAY -> sunday;
        };
    }
    // Getters and Setters
    public String getServiceId() { return serviceId; }
    public void setServiceId(String serviceId) { this.serviceId = serviceId; }
    public boolean isMonday() { return monday; }
    public void setMonday(boolean monday) { this.monday = monday; }
    public boolean isTuesday() { return tuesday; }
    public void setTuesday(boolean tuesday) { this.tuesday = tuesday; }
    public boolean isWednesday() { return wednesday; }
    public void setWednesday(boolean wednesday) { this.wednesday = wednesday; }
    public boolean isThursday() { return thursday; }
    public void setThursday(boolean thursday) { this.thursday = thursday; }
    public boolean isFriday() { return friday; }
    public void setFriday(boolean friday) { this.friday = friday; }
    public boolean isSaturday() { return saturday; }
    public void setSaturday(boolean saturday) { this.saturday = saturday; }
    public boolean isSunday() { return sunday; }
    public void setSunday(boolean sunday) { this.sunday = sunday; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = LocalDate.parse(startDate, GTFS_DATE_FORMAT); }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = LocalDate.parse(endDate, GTFS_DATE_FORMAT); }
}
