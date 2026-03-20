// package com.Major.majorProject.dto;

// import lombok.AllArgsConstructor;
// import lombok.Getter;
// import lombok.NoArgsConstructor;
// import lombok.Setter;

// import java.time.LocalTime;
// import java.util.List;

// @Getter
// @Setter
// @NoArgsConstructor
// @AllArgsConstructor
// public class SlotDto {
//     private List<LocalTime> startTime;
//     private Long pcId;
// }


package com.Major.majorProject.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class SlotDto {

    private List<LocalTime> startTime;
    private Long id;
    private Long pcId; // We only expose the ID of the related PC
    private LocalTime endTime;
    private boolean isBooked;

    public List<LocalTime> getStartTime() {
        return startTime;
    }

    public void setStartTime(List<LocalTime> startTime) {
        this.startTime = startTime;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPcId() {
        return pcId;
    }

    public void setPcId(Long pcId) {
        this.pcId = pcId;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public boolean isBooked() {
        return isBooked;
    }

    public void setBooked(boolean booked) {
        isBooked = booked;
    }
}