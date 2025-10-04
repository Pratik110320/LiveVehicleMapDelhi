package com.spring.Live.Vehicle.Map.Delhi.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class CalendarDateDto {
    private static final DateTimeFormatter GTFS_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private String serviceId;
    private LocalDate date;
    private int exceptionType; // 1 for added, 2 for removed

    // Getters and Setters
    public String getServiceId() { return serviceId; }
    public void setServiceId(String serviceId) { this.serviceId = serviceId; }
    public LocalDate getDate() { return date; }
    public void setDate(String date) { this.date = LocalDate.parse(date, GTFS_DATE_FORMAT); }
    public int getExceptionType() { return exceptionType; }
    public void setExceptionType(int exceptionType) { this.exceptionType = exceptionType; }
}
