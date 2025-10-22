package com.spring.Live.Vehicle.Map.Delhi.model;

import java.io.Serializable;
import java.util.Objects;

public class StopTimeId implements Serializable {
    private String tripId;
    private String stopId;

    // Default constructor
    public StopTimeId() {}

    // Parameterized constructor
    public StopTimeId(String tripId, String stopId) {
        this.tripId = tripId;
        this.stopId = stopId;
    }

    // Getters and Setters
    public String getTripId() {
        return tripId;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }

    public String getStopId() {
        return stopId;
    }

    public void setStopId(String stopId) {
        this.stopId = stopId;
    }

    // equals() method
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StopTimeId that = (StopTimeId) o;
        return Objects.equals(tripId, that.tripId) &&
                Objects.equals(stopId, that.stopId);
    }

    // hashCode() method
    @Override
    public int hashCode() {
        return Objects.hash(tripId, stopId);
    }

    // toString() method (optional but recommended)
    @Override
    public String toString() {
        return "StopTimeId{" +
                "tripId='" + tripId + '\'' +
                ", stopId='" + stopId + '\'' +
                '}';
    }
}