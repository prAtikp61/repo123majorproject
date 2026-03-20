package com.Major.majorProject.dto;

public class MonthlyBookingDto {
    private String month;
    private Long count;

    /**
     * No-argument constructor required by Hibernate/JPA for projections.
     */
    public MonthlyBookingDto() {}

    /**
     * This constructor is called by the JPA query.
     * We are using Integer for the month number this time.
     *
     * @param monthNumber The month number (1 for Jan) as an Integer object
     * @param count The total bookings for that month as a Long object
     */
    public MonthlyBookingDto(Integer monthNumber, Long count) { // <-- Changed back to Integer
        this.month = getMonthName(monthNumber); // Uses the Integer directly
        this.count = count;
    }

    // Helper method to convert month number to a short name
    private String getMonthName(Integer monthNumber) { // <-- Changed back to Integer
        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        // Added null check for safety
        if (monthNumber != null && monthNumber >= 1 && monthNumber <= 12) {
            return months[monthNumber - 1];
        }
        return "Unknown";
    }

    // Getters and Setters
    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }
}