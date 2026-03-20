package com.Major.majorProject.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat; // Added import
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalTime;


public class CafeAdditionDto {
    private long id;
    private String name;
    private String address;
    @DateTimeFormat(pattern = "HH:mm") // Added annotation
    private LocalTime openTime;
    @DateTimeFormat(pattern = "HH:mm") // Added annotation
    private LocalTime closeTime;
    private double hourlyRate;
    private int availablePcs;
    private MultipartFile cafeImageFile; // Renamed from cafeImage
    private String cafeImage;

    public long getId() {
        return id;
    }

    public void setId(long id) {
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

    public double getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(double hourlyRate) {
        this.hourlyRate = hourlyRate;
    }

    public int getAvailablePcs() {
        return availablePcs;
    }

    public void setAvailablePcs(int availablePcs) {
        this.availablePcs = availablePcs;
    }

    public MultipartFile getCafeImageFile() {
        return cafeImageFile;
    }

    public void setCafeImageFile(MultipartFile cafeImageFile) {
        this.cafeImageFile = cafeImageFile;
    }

    public String getCafeImage() {
        return cafeImage;
    }

    public void setCafeImage(String cafeImage) {
        this.cafeImage = cafeImage;
    }
}