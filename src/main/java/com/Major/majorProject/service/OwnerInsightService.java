package com.Major.majorProject.service;

import com.Major.majorProject.dto.HourlyBookingDto;
import com.Major.majorProject.dto.OptimizationHourDto;
import com.Major.majorProject.dto.OwnerInsightDto;
import com.Major.majorProject.dto.OwnerHourlyInsightDto;
import com.Major.majorProject.entity.Cafe;
import com.Major.majorProject.entity.CafeOwner;
import com.Major.majorProject.entity.PricingRule;
import com.Major.majorProject.repository.CafeOwnerRepository;
import com.Major.majorProject.repository.CafeRepository;
import com.Major.majorProject.repository.OfflineBookingRepository;
import com.Major.majorProject.repository.UserBookingRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OwnerInsightService {

    private final UserBookingRepository userBookingRepository;
    private final OfflineBookingRepository offlineBookingRepository;
    private final PricingRuleService pricingRuleService;
    private final MlPredictionService mlPredictionService;
    private final CafeRepository cafeRepository;
    private final CafeOwnerRepository cafeOwnerRepository;

    public OwnerInsightService(UserBookingRepository userBookingRepository,
                               OfflineBookingRepository offlineBookingRepository,
                               PricingRuleService pricingRuleService,
                               MlPredictionService mlPredictionService,
                               CafeRepository cafeRepository,
                               CafeOwnerRepository cafeOwnerRepository) {
        this.userBookingRepository = userBookingRepository;
        this.offlineBookingRepository = offlineBookingRepository;
        this.pricingRuleService = pricingRuleService;
        this.mlPredictionService = mlPredictionService;
        this.cafeRepository = cafeRepository;
        this.cafeOwnerRepository = cafeOwnerRepository;
    }

    public OwnerInsightDto getOwnerInsights(Long cafeId, String source) {
        OwnerHourlyInsightDto hourlyInsights = getOwnerHourlyInsights(cafeId, source);
        Cafe cafe = getOwnedCafe(cafeId);
        PricingRule rule = pricingRuleService.getOrCreateByCafeId(cafeId);
        String normalizedSource = source == null ? "both" : source.toLowerCase();

        double expectedRevenue = hourlyInsights.getHourlyData().stream()
                .mapToDouble(OptimizationHourDto::getRevenue)
                .sum();
        int highestForecastDemand = hourlyInsights.getHourlyData().stream()
                .mapToInt(OptimizationHourDto::getRealDemand)
                .max()
                .orElse(0);
        int totalSeats = cafe.getPcs() != null ? cafe.getPcs().size() : 0;

        Map<Integer, Integer> selectedDemand = initializeDemandMap(cafe);
        for (OptimizationHourDto hour : hourlyInsights.getHourlyData()) {
            selectedDemand.put(hour.getHour(), hour.getActualDemand());
        }

        OwnerInsightDto dto = new OwnerInsightDto();
        dto.setPeakHour(hourlyInsights.getPeakHour());
        dto.setLowHour(hourlyInsights.getLowHour());
        dto.setBestSlot(hourlyInsights.getBestRevenueHour());
        dto.setExpectedRevenue(round(expectedRevenue));
        dto.setOccupancy(totalSeats > 0
                ? Math.min(100, (int) Math.round((highestForecastDemand * 100.0) / totalSeats)) + "%"
                : "0%");
        dto.setPricingSuggestion(buildPricingSuggestion(rule,
                parseHour(hourlyInsights.getPeakHour()),
                parseHour(hourlyInsights.getLowHour()),
                selectedDemand,
                normalizedSource));
        return dto;
    }

    public OwnerHourlyInsightDto getOwnerHourlyInsights(Long cafeId, String source) {
        Cafe cafe = getOwnedCafe(cafeId);
        PricingRule rule = pricingRuleService.getOrCreateByCafeId(cafeId);
        int targetDay = LocalDate.now().getDayOfWeek().getValue();
        int totalSeats = cafe.getPcs() != null ? cafe.getPcs().size() : 0;
        String normalizedSource = source == null ? "both" : source.toLowerCase();

        Map<Integer, Integer> onlineDemand = initializeDemandMap(cafe);
        Map<Integer, Integer> offlineDemand = initializeDemandMap(cafe);
        Map<Integer, Integer> combinedDemand = initializeDemandMap(cafe);

        mergeDemand(onlineDemand, userBookingRepository.findHourlyBookingCountsByCafe(
                cafeId, com.Major.majorProject.entity.UserBooking.BookingStatus.BOOKED));
        mergeDemand(offlineDemand, offlineBookingRepository.findHourlyOfflineDemandByCafe(cafeId));

        for (int hour = cafe.getOpenTime().getHour(); hour < cafe.getCloseTime().getHour(); hour++) {
            combinedDemand.put(hour, onlineDemand.getOrDefault(hour, 0) + offlineDemand.getOrDefault(hour, 0));
        }

        Map<Integer, Integer> selectedDemand = selectDemandMap(normalizedSource, onlineDemand, offlineDemand, combinedDemand);

        List<OptimizationHourDto> hourlyData = new ArrayList<>();

        for (int hour = cafe.getOpenTime().getHour(); hour < cafe.getCloseTime().getHour(); hour++) {
            int predictedDemand = mlPredictionService.predictDemand(hour, targetDay);
            int onlineActual = onlineDemand.getOrDefault(hour, 0);
            int offlineActual = offlineDemand.getOrDefault(hour, 0);
            int combinedActual = combinedDemand.getOrDefault(hour, 0);
            int totalForecastDemand = Math.max(predictedDemand, combinedActual);

            double slotPrice = calculatePrice(rule, totalForecastDemand);
            int totalBillableSeats = totalSeats > 0 ? Math.min(totalForecastDemand, totalSeats) : totalForecastDemand;
            double totalSlotRevenue = round(slotPrice * totalBillableSeats);

            double onlineShare = combinedActual > 0 ? (double) onlineActual / combinedActual : 0;
            double offlineShare = combinedActual > 0 ? (double) offlineActual / combinedActual : 0;

            double selectedSlotRevenue;
            int selectedForecastDemand;

            if ("online".equals(normalizedSource)) {
                selectedSlotRevenue = totalSlotRevenue * onlineShare;
                selectedForecastDemand = (int) Math.round(totalForecastDemand * onlineShare);
            } else if ("offline".equals(normalizedSource)) {
                selectedSlotRevenue = totalSlotRevenue * offlineShare;
                selectedForecastDemand = (int) Math.round(totalForecastDemand * offlineShare);
            } else {
                selectedSlotRevenue = totalSlotRevenue;
                selectedForecastDemand = totalForecastDemand;
            }

            OptimizationHourDto dto = new OptimizationHourDto();
            dto.setHour(hour);
            dto.setLabel(formatHour(hour));
            dto.setPredictedDemand(predictedDemand);
            dto.setActualDemand(selectedDemand.getOrDefault(hour, 0));
            dto.setRealDemand(selectedForecastDemand);
            dto.setPrice(slotPrice);
            dto.setRevenue(round(selectedSlotRevenue));
            hourlyData.add(dto);
        }

        for (int index = 0; index < hourlyData.size(); index++) {
            hourlyData.get(index).setRealDemand(smoothDemand(hourlyData, index));
        }

        int peakHour = hourlyData.stream()
                .max((left, right) -> Integer.compare(left.getRealDemand(), right.getRealDemand()))
                .map(OptimizationHourDto::getHour)
                .orElse(cafe.getOpenTime().getHour());

        int lowHour = hourlyData.stream()
                .min((left, right) -> Integer.compare(left.getRealDemand(), right.getRealDemand()))
                .map(OptimizationHourDto::getHour)
                .orElse(cafe.getOpenTime().getHour());

        int bestSlotHour = hourlyData.stream()
                .max((left, right) -> Double.compare(left.getRevenue(), right.getRevenue()))
                .map(OptimizationHourDto::getHour)
                .orElse(peakHour);

        OwnerHourlyInsightDto dto = new OwnerHourlyInsightDto();
        dto.setPeakHour(formatHour(peakHour));
        dto.setLowHour(formatHour(lowHour));
        dto.setBestRevenueHour(formatHour(bestSlotHour));
        dto.setHourlyData(hourlyData);
        return dto;
    }

    private Map<Integer, Integer> initializeDemandMap(Cafe cafe) {
        Map<Integer, Integer> demandMap = new HashMap<>();
        for (int hour = cafe.getOpenTime().getHour(); hour < cafe.getCloseTime().getHour(); hour++) {
            demandMap.put(hour, 0);
        }
        return demandMap;
    }

    private Map<Integer, Integer> selectDemandMap(String source,
                                                  Map<Integer, Integer> onlineDemand,
                                                  Map<Integer, Integer> offlineDemand,
                                                  Map<Integer, Integer> combinedDemand) {
        if ("online".equals(source)) {
            return onlineDemand;
        }
        if ("offline".equals(source)) {
            return offlineDemand;
        }
        return combinedDemand;
    }

    private int smoothDemand(List<OptimizationHourDto> hourlyData, int index) {
        int previous = index > 0 ? hourlyData.get(index - 1).getRealDemand() : hourlyData.get(index).getRealDemand();
        int current = hourlyData.get(index).getRealDemand();
        int next = index < hourlyData.size() - 1 ? hourlyData.get(index + 1).getRealDemand() : current;
        return (int) Math.round((previous + current + next) / 3.0);
    }

    private void mergeDemand(Map<Integer, Integer> demandMap, List<HourlyBookingDto> rows) {
        for (HourlyBookingDto row : rows) {
            if (demandMap.containsKey(row.getHour())) {
                demandMap.put(row.getHour(), demandMap.get(row.getHour()) + row.getCount().intValue());
            }
        }
    }

    private double calculatePrice(PricingRule rule, int demand) {
        if (demand < rule.getLowDemandThreshold()) {
            return round(rule.getBasePrice() * rule.getLowMultiplier());
        }
        if (demand > rule.getHighDemandThreshold()) {
            return round(rule.getBasePrice() * rule.getHighMultiplier());
        }
        return round(rule.getBasePrice());
    }

    private String buildPricingSuggestion(PricingRule rule, int peakHour, int lowHour, Map<Integer, Integer> demandMap, String source) {
        String demandLabel = "both".equals(source) ? "combined demand" : source + " demand";
        if (demandMap.getOrDefault(peakHour, 0) > rule.getHighDemandThreshold()) {
            int increasePercent = (int) Math.round((rule.getHighMultiplier() - 1) * 100);
            return "Increase price by about " + increasePercent + "% around " + formatHour(peakHour) + " based on " + demandLabel + ".";
        }
        if (demandMap.getOrDefault(lowHour, 0) < rule.getLowDemandThreshold()) {
            int discountPercent = (int) Math.round((1 - rule.getLowMultiplier()) * 100);
            return "Offer a " + discountPercent + "% discount around " + formatHour(lowHour) + " based on " + demandLabel + ".";
        }
        return "Keep standard pricing. " + Character.toUpperCase(demandLabel.charAt(0)) + demandLabel.substring(1) + " is currently balanced across most slots.";
    }

    private String formatHour(int hour) {
        int normalized = hour % 24;
        int displayHour = normalized % 12 == 0 ? 12 : normalized % 12;
        String meridiem = normalized < 12 ? "AM" : "PM";
        return displayHour + " " + meridiem;
    }

    private int parseHour(String label) {
        String[] parts = label.split(" ");
        int hour = Integer.parseInt(parts[0]);
        String meridiem = parts[1];
        if ("PM".equals(meridiem) && hour != 12) {
            return hour + 12;
        }
        if ("AM".equals(meridiem) && hour == 12) {
            return 0;
        }
        return hour;
    }

    private double round(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    private Cafe getOwnedCafe(Long cafeId) {
        String ownerEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        CafeOwner owner = cafeOwnerRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new RuntimeException("Owner not found"));
        Cafe cafe = cafeRepository.findById(cafeId)
                .orElseThrow(() -> new RuntimeException("Cafe not found"));
        if (cafe.getOwner() == null || !cafe.getOwner().getId().equals(owner.getId())) {
            throw new RuntimeException("You do not have access to this cafe.");
        }
        return cafe;
    }
}
