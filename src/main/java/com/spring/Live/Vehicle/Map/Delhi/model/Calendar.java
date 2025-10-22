package com.spring.Live.Vehicle.Map.Delhi.model;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "calendar")
public class Calendar {

    @Id
    @Column(name = "service_id")
    private String serviceId;

    @Column(name = "start_date")
    private String startDate;

    @Column(name = "end_date")
    private String endDate;

    private int monday;
    private int tuesday;
    private int wednesday;
    private int thursday;
    private int friday;
    private int saturday;
    private int sunday;

    public String getServiceId() { return serviceId; }
    public void setServiceId(String serviceId) { this.serviceId = serviceId; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }

    public int getMonday() { return monday; }
    public void setMonday(int monday) { this.monday = monday; }

    public int getTuesday() { return tuesday; }
    public void setTuesday(int tuesday) { this.tuesday = tuesday; }

    public int getWednesday() { return wednesday; }
    public void setWednesday(int wednesday) { this.wednesday = wednesday; }

    public int getThursday() { return thursday; }
    public void setThursday(int thursday) { this.thursday = thursday; }

    public int getFriday() { return friday; }
    public void setFriday(int friday) { this.friday = friday; }

    public int getSaturday() { return saturday; }
    public void setSaturday(int saturday) { this.saturday = saturday; }

    public int getSunday() { return sunday; }
    public void setSunday(int sunday) { this.sunday = sunday; }
}
