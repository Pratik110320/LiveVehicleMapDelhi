package com.spring.Live.Vehicle.Map.Delhi.model;

import com.opencsv.bean.CsvBindByName;
import jakarta.persistence.*;

// ===============================
// STOP TIME ENTITY
// ===============================
@Entity
@Table(name = "stop_times", indexes = {
        @Index(name = "idx_stoptime_trip_id", columnList = "trip_id")
})
public class StopTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CsvBindByName(column = "trip_id")
    @Column(name = "trip_id")
    private String tripId;

    @CsvBindByName(column = "arrival_time")
    @Column(name = "arrival_time")
    private String arrivalTime;

    @CsvBindByName(column = "departure_time")
    @Column(name = "departure_time")
    private String departureTime;

    @CsvBindByName(column = "stop_id")
    @Column(name = "stop_id")
    private String stopId;

    @CsvBindByName(column = "stop_sequence")
    @Column(name = "stop_sequence")
    private int stopSequence;

    public StopTime() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTripId() {
        return tripId;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }

    public String getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(String arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public String getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(String departureTime) {
        this.departureTime = departureTime;
    }

    public String getStopId() {
        return stopId;
    }

    public void setStopId(String stopId) {
        this.stopId = stopId;
    }

    public int getStopSequence() {
        return stopSequence;
    }

    public void setStopSequence(int stopSequence) {
        this.stopSequence = stopSequence;
    }
}
