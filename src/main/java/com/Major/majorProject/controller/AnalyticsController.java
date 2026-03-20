package com.Major.majorProject.controller;

import com.Major.majorProject.dto.*;
import com.Major.majorProject.service.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @Autowired
    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/games/{cafeId}")
    public ResponseEntity<List<GameCountDto>> getMostPreferredGamesForCafe(@PathVariable Long cafeId) {
        List<GameCountDto> games = analyticsService.getMostPreferredGamesForCafe(cafeId);
        return ResponseEntity.ok(games);
    }

    // New endpoint for monthly booking data
    @GetMapping("/bookings/monthly/{cafeId}")
    public ResponseEntity<List<MonthlyBookingDto>> getMonthlyBookingPattern(@PathVariable Long cafeId) {
        List<MonthlyBookingDto> bookingData = analyticsService.getMonthlyBookingPatternForCafe(cafeId);
        return ResponseEntity.ok(bookingData);
    }


    @GetMapping("/bookings/hourly/{cafeId}")
    public ResponseEntity<List<HourlyBookingDto>> getHourlyBookingPattern(@PathVariable Long cafeId) {
        List<HourlyBookingDto> bookingData = analyticsService.getHourlyBookingPatternForCafe(cafeId);
        return ResponseEntity.ok(bookingData);
    }

    @GetMapping("/utilization/pc/{cafeId}")
    public ResponseEntity<List<PcUtilizationDto>> getPcUtilization(@PathVariable Long cafeId) {
        List<PcUtilizationDto> utilizationData = analyticsService.getPcUtilizationForCafe(cafeId);
        return ResponseEntity.ok(utilizationData);
    }
    @GetMapping("/revenue/pc/{cafeId}")
    public ResponseEntity<List<PcRevenueDto>> getPcRevenue(@PathVariable Long cafeId) {
        List<PcRevenueDto> revenueData = analyticsService.getPcRevenueForCafe(cafeId);
        return ResponseEntity.ok(revenueData);
    }
}