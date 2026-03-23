package com.Major.majorProject.dto;

import java.util.List;

public class OptimizeResponseDto {
    private int predictedDemand;
    private int onlineBookings;
    private int offlineBookings;
    private int actualDemand;
    private int realDemand;
    private String demandLevel;
    private double price;
    private String message;
    private int suggestedDuration;
    private int selectedHour;
    private String selectedHourLabel;
    private String recommendedHour;
    private double recommendedPrice;
    private int recommendedDemand;
    private List<OptimizationHourDto> recommendedWindow;
    private List<OptimizationHourDto> hourlyData;

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

    public int getActualDemand() {
        return actualDemand;
    }

    public void setActualDemand(int actualDemand) {
        this.actualDemand = actualDemand;
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

    public int getSelectedHour() {
        return selectedHour;
    }

    public void setSelectedHour(int selectedHour) {
        this.selectedHour = selectedHour;
    }

    public String getSelectedHourLabel() {
        return selectedHourLabel;
    }

    public void setSelectedHourLabel(String selectedHourLabel) {
        this.selectedHourLabel = selectedHourLabel;
    }

    public String getRecommendedHour() {
        return recommendedHour;
    }

    public void setRecommendedHour(String recommendedHour) {
        this.recommendedHour = recommendedHour;
    }

    public double getRecommendedPrice() {
        return recommendedPrice;
    }

    public void setRecommendedPrice(double recommendedPrice) {
        this.recommendedPrice = recommendedPrice;
    }

    public int getRecommendedDemand() {
        return recommendedDemand;
    }

    public void setRecommendedDemand(int recommendedDemand) {
        this.recommendedDemand = recommendedDemand;
    }

    public List<OptimizationHourDto> getRecommendedWindow() {
        return recommendedWindow;
    }

    public void setRecommendedWindow(List<OptimizationHourDto> recommendedWindow) {
        this.recommendedWindow = recommendedWindow;
    }

    public List<OptimizationHourDto> getHourlyData() {
        return hourlyData;
    }

    public void setHourlyData(List<OptimizationHourDto> hourlyData) {
        this.hourlyData = hourlyData;
    }
}
