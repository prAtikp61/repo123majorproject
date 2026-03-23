package com.Major.majorProject.dto;

public class OptimizeResponseDto {
    private int predictedDemand;
    private int onlineBookings;
    private int offlineBookings;
    private int realDemand;
    private String demandLevel;
    private double price;
    private String message;
    private int suggestedDuration;

    public int getPredictedDemand() {
        return predictedDemand;
    }

    public void setPredictedDemand(int predictedDemand) {
        this.predictedDemand = predictedDemand;
    }

    public int getOnlineBookings() {
        return onlineBookings;
    }

    public void setOnlineBookings(int onlineBookings) {
        this.onlineBookings = onlineBookings;
    }

    public int getOfflineBookings() {
        return offlineBookings;
    }

    public void setOfflineBookings(int offlineBookings) {
        this.offlineBookings = offlineBookings;
    }

    public int getRealDemand() {
        return realDemand;
    }

    public void setRealDemand(int realDemand) {
        this.realDemand = realDemand;
    }

    public String getDemandLevel() {
        return demandLevel;
    }

    public void setDemandLevel(String demandLevel) {
        this.demandLevel = demandLevel;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getSuggestedDuration() {
        return suggestedDuration;
    }

    public void setSuggestedDuration(int suggestedDuration) {
        this.suggestedDuration = suggestedDuration;
    }
}
