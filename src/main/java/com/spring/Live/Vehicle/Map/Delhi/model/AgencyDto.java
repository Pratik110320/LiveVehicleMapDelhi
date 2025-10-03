package com.spring.Live.Vehicle.Map.Delhi.model;

import com.opencsv.bean.CsvBindByName;

public class AgencyDto {
    @CsvBindByName(column = "agency_id")
    private String agencyId;
    @CsvBindByName(column = "agency_name")
    private String agencyName;
    @CsvBindByName(column = "agency_url")
    private String agencyUrl;
    @CsvBindByName(column = "agency_timezone")
    private String agencyTimezone;

    // Getters and Setters
    public String getAgencyId() { return agencyId; }
    public void setAgencyId(String agencyId) { this.agencyId = agencyId; }
    public String getAgencyName() { return agencyName; }
    public void setAgencyName(String agencyName) { this.agencyName = agencyName; }
    public String getAgencyUrl() { return agencyUrl; }
    public void setAgencyUrl(String agencyUrl) { this.agencyUrl = agencyUrl; }
    public String getAgencyTimezone() { return agencyTimezone; }
    public void setAgencyTimezone(String agencyTimezone) { this.agencyTimezone = agencyTimezone; }
}
