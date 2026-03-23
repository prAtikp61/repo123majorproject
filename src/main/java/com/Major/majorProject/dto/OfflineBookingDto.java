package com.Major.majorProject.dto;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public class OfflineBookingDto {
    private Long cafeId;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate bookingDate;

    private Integer timeSlot;
    private Integer customerCount;
    private String notes;

    public Long getCafeId() {
        return cafeId;
    }

    public void setCafeId(Long cafeId) {
        this.cafeId = cafeId;
    }

    public LocalDate getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(LocalDate bookingDate) {
        this.bookingDate = bookingDate;
    }

    public Integer getTimeSlot() {
        return timeSlot;
    }

    public void setTimeSlot(Integer timeSlot) {
        this.timeSlot = timeSlot;
    }

    public Integer getCustomerCount() {
        return customerCount;
    }

    public void setCustomerCount(Integer customerCount) {
        this.customerCount = customerCount;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
