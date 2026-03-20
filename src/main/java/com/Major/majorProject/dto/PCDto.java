package com.Major.majorProject.dto;

import lombok.Getter;
import lombok.Setter;


public class PCDto {
    private long id;
    private int seatNumber;
    private String configuration;
    private String available;
    private long CafeId;
    private String cafeName;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(int seatNumber) {
        this.seatNumber = seatNumber;
    }

    public String getConfiguration() {
        return configuration;
    }

    public void setConfiguration(String configuration) {
        this.configuration = configuration;
    }

    public String getAvailable() {
        return available;
    }

    public void setAvailable(String available) {
        this.available = available;
    }

    public long getCafeId() {
        return CafeId;
    }

    public void setCafeId(long cafeId) {
        CafeId = cafeId;
    }

    public String getCafeName() {
        return cafeName;
    }

    public void setCafeName(String cafeName) {
        this.cafeName = cafeName;
    }
}
