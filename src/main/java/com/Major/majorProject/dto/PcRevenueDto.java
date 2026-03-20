package com.Major.majorProject.dto;

public class PcRevenueDto {
    private int seatNumber;
    private double totalRevenue;
    private long bookedHours; // Store the count of booked hours

    public PcRevenueDto(int seatNumber, long bookedHours, double hourlyRate) {
        this.seatNumber = seatNumber;
        this.bookedHours = bookedHours;
        this.totalRevenue = bookedHours * hourlyRate;
    }

    // Getters and Setters
    public int getSeatNumber() { return seatNumber; }
    public void setSeatNumber(int seatNumber) { this.seatNumber = seatNumber; }
    public double getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(double totalRevenue) { this.totalRevenue = totalRevenue; }
    public long getBookedHours() { return bookedHours; }
    public void setBookedHours(long bookedHours) { this.bookedHours = bookedHours; }
}