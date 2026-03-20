package com.Major.majorProject.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalTime;
import java.util.List;

@Entity

public class Cafe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String address;

    private LocalTime openTime;

    private LocalTime closeTime;

    private Double hourlyRate;

    private String cafeImage;

    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private CafeOwner owner;

    @OneToMany(mappedBy = "cafe", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PC> pcs;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public LocalTime getOpenTime() {
        return openTime;
    }

    public void setOpenTime(LocalTime openTime) {
        this.openTime = openTime;
    }

    public LocalTime getCloseTime() {
        return closeTime;
    }

    public void setCloseTime(LocalTime closeTime) {
        this.closeTime = closeTime;
    }

    public Double getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(Double hourlyRate) {
        this.hourlyRate = hourlyRate;
    }

    public String getCafeImage() {
        return cafeImage;
    }

    public void setCafeImage(String cafeImage) {
        this.cafeImage = cafeImage;
    }

    public CafeOwner getOwner() {
        return owner;
    }

    public void setOwner(CafeOwner owner) {
        this.owner = owner;
    }

    public List<PC> getPcs() {
        return pcs;
    }

    public void setPcs(List<PC> pcs) {
        this.pcs = pcs;
    }
}

