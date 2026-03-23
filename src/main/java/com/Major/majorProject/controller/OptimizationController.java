package com.Major.majorProject.controller;

import com.Major.majorProject.dto.OptimizeResponseDto;
import com.Major.majorProject.dto.OptimizationHourDto;
import com.Major.majorProject.service.OptimizationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
public class OptimizationController {

    private final OptimizationService optimizationService;

    public OptimizationController(OptimizationService optimizationService) {
        this.optimizationService = optimizationService;
    }

    @GetMapping("/optimize")
    public ResponseEntity<OptimizeResponseDto> optimize(@RequestParam("cafeId") Long cafeId,
                                                        @RequestParam("timeSlot") int timeSlot,
                                                        @RequestParam(value = "day", required = false) Integer day,
                                                        @RequestParam(value = "bookingDate", required = false) LocalDate bookingDate) {
        return ResponseEntity.ok(optimizationService.optimize(cafeId, timeSlot, day, bookingDate));
    }

    @GetMapping("/optimize/hourly")
    public ResponseEntity<List<OptimizationHourDto>> getHourlyPredictions(@RequestParam("cafeId") Long cafeId,
                                                                          @RequestParam(value = "day", required = false) Integer day,
                                                                          @RequestParam(value = "bookingDate", required = false) LocalDate bookingDate) {
        return ResponseEntity.ok(optimizationService.getHourlyPredictions(cafeId, bookingDate, day));
    }
}
