package com.Major.majorProject.dto;

public class PricingRuleDto {
    private Long cafeId;
    private Double basePrice;
    private Integer lowDemandThreshold;
    private Integer highDemandThreshold;
    private Double lowMultiplier;
    private Double highMultiplier;

    public Long getCafeId() {
        return cafeId;
    }

    public void setCafeId(Long cafeId) {
        this.cafeId = cafeId;
    }

    public Double getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(Double basePrice) {
        this.basePrice = basePrice;
    }

    public Integer getLowDemandThreshold() {
        return lowDemandThreshold;
    }

    public void setLowDemandThreshold(Integer lowDemandThreshold) {
        this.lowDemandThreshold = lowDemandThreshold;
    }

    public Integer getHighDemandThreshold() {
        return highDemandThreshold;
    }

    public void setHighDemandThreshold(Integer highDemandThreshold) {
        this.highDemandThreshold = highDemandThreshold;
    }

    public Double getLowMultiplier() {
        return lowMultiplier;
    }

    public void setLowMultiplier(Double lowMultiplier) {
        this.lowMultiplier = lowMultiplier;
    }

    public Double getHighMultiplier() {
        return highMultiplier;
    }

    public void setHighMultiplier(Double highMultiplier) {
        this.highMultiplier = highMultiplier;
    }
}
