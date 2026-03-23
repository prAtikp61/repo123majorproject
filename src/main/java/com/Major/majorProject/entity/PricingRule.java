package com.Major.majorProject.entity;

import jakarta.persistence.*;

@Entity
public class PricingRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cafe_id", nullable = false, unique = true)
    private Cafe cafe;

    @Column(nullable = false)
    private Double basePrice;

    @Column(nullable = false)
    private Integer lowDemandThreshold;

    @Column(nullable = false)
    private Integer highDemandThreshold;

    @Column(nullable = false)
    private Double lowMultiplier;

    @Column(nullable = false)
    private Double highMultiplier;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Cafe getCafe() {
        return cafe;
    }

    public void setCafe(Cafe cafe) {
        this.cafe = cafe;
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
