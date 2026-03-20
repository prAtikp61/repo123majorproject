package com.Major.majorProject.dto;

public class PcBookingCountDto {
    private int seatNumber;
    private Long count;

    public PcBookingCountDto(int seatNumber, Long count) {
        this.seatNumber = seatNumber;
        this.count = count;
    }

    // Getters
    public int getSeatNumber() { return seatNumber; }
    public Long getCount() { return count; }
}