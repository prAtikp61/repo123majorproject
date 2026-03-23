package com.Major.majorProject.dto;

public class OptimizationHourDto {
    private int hour;
    private String label;
    private int predictedDemand;
    private int actualDemand;
    private int realDemand;
    private double price;
    private double revenue;
    private double score;

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public int getPredictedDemand() {
        return predictedDemand;
    }

    public void setPredictedDemand(int predictedDemand) {
        this.predictedDemand = predictedDemand;
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

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public double getRevenue() {
        return revenue;
    }

    public void setRevenue(double revenue) {
        this.revenue = revenue;
    }
}
