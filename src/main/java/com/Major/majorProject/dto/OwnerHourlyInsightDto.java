package com.Major.majorProject.dto;

import java.util.List;

public class OwnerHourlyInsightDto {
    private String peakHour;
    private String lowHour;
    private String bestRevenueHour;
    private List<OptimizationHourDto> hourlyData;

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

    public String getBestRevenueHour() {
        return bestRevenueHour;
    }

    public void setBestRevenueHour(String bestRevenueHour) {
        this.bestRevenueHour = bestRevenueHour;
    }

    public List<OptimizationHourDto> getHourlyData() {
        return hourlyData;
    }

    public void setHourlyData(List<OptimizationHourDto> hourlyData) {
        this.hourlyData = hourlyData;
    }
}
