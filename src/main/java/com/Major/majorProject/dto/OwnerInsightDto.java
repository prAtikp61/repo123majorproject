package com.Major.majorProject.dto;

public class OwnerInsightDto {
    private String peakHour;
    private String lowHour;
    private String bestSlot;
    private double expectedRevenue;
    private String occupancy;
    private String pricingSuggestion;

    public String getPeakHour() {
        return peakHour;
    }

    public void setPeakHour(String peakHour) {
        this.peakHour = peakHour;
    }

    public String getLowHour() {
        return lowHour;
    }

    public void setLowHour(String lowHour) {
        this.lowHour = lowHour;
    }

    public String getBestSlot() {
        return bestSlot;
    }

    public void setBestSlot(String bestSlot) {
        this.bestSlot = bestSlot;
    }

    public double getExpectedRevenue() {
        return expectedRevenue;
    }

    public void setExpectedRevenue(double expectedRevenue) {
        this.expectedRevenue = expectedRevenue;
    }

    public String getOccupancy() {
        return occupancy;
    }

    public void setOccupancy(String occupancy) {
        this.occupancy = occupancy;
    }

    public String getPricingSuggestion() {
        return pricingSuggestion;
    }

    public void setPricingSuggestion(String pricingSuggestion) {
        this.pricingSuggestion = pricingSuggestion;
    }
}
