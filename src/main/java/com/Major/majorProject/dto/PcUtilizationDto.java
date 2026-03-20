package com.Major.majorProject.dto;

public class PcUtilizationDto {
    private int seatNumber;
    private double utilizationPercentage; // e.g., 75.0 for 75%
    private long bookedSlots;
    private long totalSlots;

    public PcUtilizationDto(int seatNumber, long bookedSlots, long totalSlots) {
        this.seatNumber = seatNumber;
        this.bookedSlots = bookedSlots;
        this.totalSlots = totalSlots;
        this.utilizationPercentage = (totalSlots > 0) ? ((double) bookedSlots / totalSlots) * 100.0 : 0.0;
    }

    // Getters and Setters
    public int getSeatNumber() { return seatNumber; }
    public void setSeatNumber(int seatNumber) { this.seatNumber = seatNumber; }
    public double getUtilizationPercentage() { return utilizationPercentage; }
    public void setUtilizationPercentage(double utilizationPercentage) { this.utilizationPercentage = utilizationPercentage; }
    public long getBookedSlots() { return bookedSlots; }
    public void setBookedSlots(long bookedSlots) { this.bookedSlots = bookedSlots; }
    public long getTotalSlots() { return totalSlots; }
    public void setTotalSlots(long totalSlots) { this.totalSlots = totalSlots; }
}