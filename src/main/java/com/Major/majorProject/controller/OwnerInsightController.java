package com.Major.majorProject.controller;

import com.Major.majorProject.dto.OwnerInsightDto;
import com.Major.majorProject.dto.OwnerHourlyInsightDto;
import com.Major.majorProject.service.OwnerInsightService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class OwnerInsightController {

    private final OwnerInsightService ownerInsightService;

    public OwnerInsightController(OwnerInsightService ownerInsightService) {
        this.ownerInsightService = ownerInsightService;
    }

    @GetMapping("/owner-insights")
    public ResponseEntity<OwnerInsightDto> getOwnerInsights(@RequestParam("cafeId") Long cafeId,
                                                            @RequestParam(value = "source", defaultValue = "both") String source) {
        return ResponseEntity.ok(ownerInsightService.getOwnerInsights(cafeId, source));
    }

    @GetMapping("/owner-hourly-insights")
    public ResponseEntity<OwnerHourlyInsightDto> getOwnerHourlyInsights(@RequestParam("cafeId") Long cafeId,
                                                                        @RequestParam(value = "source", defaultValue = "both") String source) {
        return ResponseEntity.ok(ownerInsightService.getOwnerHourlyInsights(cafeId, source));
    }
}
