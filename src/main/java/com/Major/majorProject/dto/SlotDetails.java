package com.Major.majorProject.dto;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalTime;


public class SlotDetails {
    private Long id;
    private LocalTime startTime;
    private LocalTime endTime;
    private String status;
    private long cafeId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getCafeId() {
        return cafeId;
    }

    public void setCafeId(long cafeId) {
        this.cafeId = cafeId;
    }
}