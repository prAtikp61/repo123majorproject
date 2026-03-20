package com.Major.majorProject.dto;

public class HourlyBookingDto {
    private int hour; // 0-23 representing the hour
    private Long count;

    // Default constructor (important for JPA/Hibernate)
    public HourlyBookingDto() {}

    /**
     * Constructor for JPA query.
     * Changed 'Integer hour' to 'int hour' to match the expected return type
     * of the HOUR() database function.
     */
    public HourlyBookingDto(Integer hour, Long count) { // <-- Changed from Integer to int
        this.hour = hour;
        this.count = count;
    }

    // Getters and Setters
    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }
}