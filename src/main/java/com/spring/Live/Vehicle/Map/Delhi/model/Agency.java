package com.spring.Live.Vehicle.Map.Delhi.model;

import com.opencsv.bean.CsvBindByName;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;


@Entity
@Table(name = "agency")
public class Agency {

    @Id
    @CsvBindByName(column = "agency_id")
    @Column(name = "agency_id")
    private String agencyId;

    @CsvBindByName(column = "agency_name")
    @Column(name = "agency_name")
    private String agencyName;

    @CsvBindByName(column = "agency_url")
    @Column(name = "agency_url")
    private String agencyUrl;

    @CsvBindByName(column = "agency_timezone")
    @Column(name = "agency_timezone")
    private String agencyTimezone;

    @CsvBindByName(column = "agency_lang")
    @Column(name = "agency_lang")
    private String agencyLang;

    public Agency() {
    }

    public Agency(String agencyId, String agencyName, String agencyUrl, String agencyTimezone, String agencyLang) {
        this.agencyId = agencyId;
        this.agencyName = agencyName;
        this.agencyUrl = agencyUrl;
        this.agencyTimezone = agencyTimezone;
        this.agencyLang = agencyLang;
    }

    public String getAgencyId() {
        return agencyId;
    }

    public void setAgencyId(String agencyId) {
        this.agencyId = agencyId;
    }

    public String getAgencyName() {
        return agencyName;
    }

    public void setAgencyName(String agencyName) {
        this.agencyName = agencyName;
    }

    public String getAgencyUrl() {
        return agencyUrl;
    }

    public void setAgencyUrl(String agencyUrl) {
        this.agencyUrl = agencyUrl;
    }

    public String getAgencyTimezone() {
        return agencyTimezone;
    }

    public void setAgencyTimezone(String agencyTimezone) {
        this.agencyTimezone = agencyTimezone;
    }

    public String getAgencyLang() {
        return agencyLang;
    }

    public void setAgencyLang(String agencyLang) {
        this.agencyLang = agencyLang;
    }
}
