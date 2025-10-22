package com.spring.Live.Vehicle.Map.Delhi.model;

import jakarta.persistence.*;

@Entity
@Table(name = "stop_times")
@IdClass(StopTimeId.class)
public class StopTime {

    @Id
    @Column(name = "trip_id")
    private String tripId;

    @Id
    @Column(name = "stop_id")
    private String stopId;

    @Column(name = "arrival_time")
    private String arrivalTime;

    @Column(name = "departure_time")
    private String departureTime;

    @Column(name = "stop_sequence")
    private int stopSequence;

    // getters and setters
    public String getTripId() { return tripId; }
    public void setTripId(String tripId) { this.tripId = tripId; }
    public String getStopId() { return stopId; }
    public void setStopId(String stopId) { this.stopId = stopId; }
    public String getArrivalTime() { return arrivalTime; }
    public void setArrivalTime(String arrivalTime) { this.arrivalTime = arrivalTime; }
    public String getDepartureTime() { return departureTime; }
    public void setDepartureTime(String departureTime) { this.departureTime = departureTime; }
    public int getStopSequence() { return stopSequence; }
    public void setStopSequence(int stopSequence) { this.stopSequence = stopSequence; }
}
