package com.Major.majorProject.service;

import com.Major.majorProject.dto.OptimizationHourDto;
import com.Major.majorProject.dto.OptimizeResponseDto;
import com.Major.majorProject.entity.Cafe;
import com.Major.majorProject.entity.PricingRule;
import com.Major.majorProject.repository.CafeRepository;
import com.Major.majorProject.repository.UserBookingRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class OptimizationService {

    private final MlPredictionService mlPredictionService;
    private final PricingRuleService pricingRuleService;
    private final OfflineBookingService offlineBookingService;
    private final UserBookingRepository userBookingRepository;
    private final CafeRepository cafeRepository;

    public OptimizationService(MlPredictionService mlPredictionService,
                               PricingRuleService pricingRuleService,
                               OfflineBookingService offlineBookingService,
                               UserBookingRepository userBookingRepository,
                               CafeRepository cafeRepository) {
        this.mlPredictionService = mlPredictionService;
        this.pricingRuleService = pricingRuleService;
        this.offlineBookingService = offlineBookingService;
        this.userBookingRepository = userBookingRepository;
        this.cafeRepository = cafeRepository;
    }

    public OptimizeResponseDto optimize(Long cafeId, int timeSlot, Integer dayOfWeek, LocalDate bookingDate) {
        LocalDate targetDate = bookingDate != null ? bookingDate : LocalDate.now();
        int targetDay = dayOfWeek != null ? dayOfWeek : targetDate.getDayOfWeek().getValue();
        Cafe cafe = cafeRepository.findById(cafeId)
                .orElseThrow(() -> new RuntimeException("Cafe not found"));
        PricingRule rule = pricingRuleService.getOrCreateByCafeId(cafeId);

        List<OptimizationHourDto> hourlyPredictions = getHourlyPredictions(cafeId, cafe, rule, targetDate, targetDay);
        OptimizationHourDto currentHour = hourlyPredictions.stream()
                .filter(hour -> hour.getHour() == timeSlot)
                .findFirst()
                .orElseGet(() -> hourlyPredictions.isEmpty() ? null : hourlyPredictions.get(0));
        List<OptimizationHourDto> nearbyWindow = hourlyPredictions.stream()
                .filter(hour -> Math.abs(hour.getHour() - timeSlot) <= 2)
                .toList();
        OptimizationHourDto bestHour = nearbyWindow.stream()
                .min(Comparator.comparingDouble(OptimizationHourDto::getScore)
                        .thenComparingInt(OptimizationHourDto::getHour))
                .orElse(currentHour);

        if (currentHour == null) {
            throw new RuntimeException("No optimization slots available for this cafe.");
        }

        String demandLevel;
        int suggestedDuration;
        String message;
        int realDemand = currentHour.getRealDemand();

        if (realDemand < rule.getLowDemandThreshold()) {
            demandLevel = "Low";
            suggestedDuration = 3;
        } else if (realDemand > rule.getHighDemandThreshold()) {
            demandLevel = "High";
            suggestedDuration = 1;
        } else {
            demandLevel = "Moderate";
            suggestedDuration = 2;
        }

        message = buildSmartMessage(currentHour, bestHour, demandLevel);

        OptimizeResponseDto response = new OptimizeResponseDto();
        response.setPredictedDemand(currentHour.getPredictedDemand());
        response.setOnlineBookings(getOnlineDemand(cafeId, targetDate, currentHour.getHour()));
        response.setOfflineBookings(offlineBookingService.getOfflineDemand(cafeId, targetDate, currentHour.getHour()));
        response.setActualDemand(currentHour.getActualDemand());
        response.setRealDemand(currentHour.getRealDemand());
        response.setDemandLevel(demandLevel);
        response.setPrice(currentHour.getPrice());
        response.setMessage(message);
        response.setSuggestedDuration(suggestedDuration);
        response.setSelectedHour(currentHour.getHour());
        response.setSelectedHourLabel(currentHour.getLabel());
        response.setRecommendedHour(formatHour(bestHour.getHour()));
        response.setRecommendedPrice(bestHour.getPrice());
        response.setRecommendedDemand(bestHour.getRealDemand());
        response.setRecommendedWindow(nearbyWindow);
        response.setHourlyData(hourlyPredictions);
        return response;
    }

    public List<OptimizationHourDto> getHourlyPredictions(Long cafeId, LocalDate bookingDate, Integer dayOfWeek) {
        LocalDate targetDate = bookingDate != null ? bookingDate : LocalDate.now();
        int targetDay = dayOfWeek != null ? dayOfWeek : targetDate.getDayOfWeek().getValue();
        Cafe cafe = cafeRepository.findById(cafeId)
                .orElseThrow(() -> new RuntimeException("Cafe not found"));
        PricingRule rule = pricingRuleService.getOrCreateByCafeId(cafeId);
        return getHourlyPredictions(cafeId, cafe, rule, targetDate, targetDay);
    }

    private List<OptimizationHourDto> getHourlyPredictions(Long cafeId,
                                                           Cafe cafe,
                                                           PricingRule rule,
                                                           LocalDate bookingDate,
                                                           int targetDay) {
        List<OptimizationHourDto> hourlyData = new ArrayList<>();

        for (int hour = cafe.getOpenTime().getHour(); hour < cafe.getCloseTime().getHour(); hour++) {
            int predictedDemand = mlPredictionService.predictDemand(hour, targetDay);
            int onlineBookings = getOnlineDemand(cafeId, bookingDate, hour);
            int offlineBookings = offlineBookingService.getOfflineDemand(cafeId, bookingDate, hour);
            int actualDemand = onlineBookings + offlineBookings;
            int rawDemand = Math.max(predictedDemand, actualDemand);

            OptimizationHourDto dto = new OptimizationHourDto();
            dto.setHour(hour);
            dto.setLabel(formatHour(hour));
            dto.setPredictedDemand(predictedDemand);
            dto.setActualDemand(actualDemand);
            dto.setRealDemand(rawDemand);
            hourlyData.add(dto);
        }

        for (int index = 0; index < hourlyData.size(); index++) {
            int smoothedDemand = smoothDemand(hourlyData, index);
            double slotPrice = calculatePrice(rule, smoothedDemand);
            double score = smoothedDemand + ((slotPrice / Math.max(rule.getBasePrice(), 1.0)) * 3.0);

            hourlyData.get(index).setRealDemand(smoothedDemand);
            hourlyData.get(index).setPrice(slotPrice);
            hourlyData.get(index).setRevenue(round(slotPrice * smoothedDemand));
            hourlyData.get(index).setScore(round(score));
        }

        return hourlyData;
    }

    private int getOnlineDemand(Long cafeId, LocalDate bookingDate, int timeSlot) {
        return Math.toIntExact(userBookingRepository.countActiveDemandForHour(
                cafeId,
                bookingDate,
                LocalTime.of(timeSlot, 0),
                LocalDateTime.now()
        ));
    }

    private int smoothDemand(List<OptimizationHourDto> hourlyData, int index) {
        int previous = index > 0 ? hourlyData.get(index - 1).getRealDemand() : hourlyData.get(index).getRealDemand();
        int current = hourlyData.get(index).getRealDemand();
        int next = index < hourlyData.size() - 1 ? hourlyData.get(index + 1).getRealDemand() : current;
        return (int) Math.round((previous + current + next) / 3.0);
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

    private String buildSmartMessage(OptimizationHourDto currentHour, OptimizationHourDto bestHour, String demandLevel) {
        if (currentHour.getHour() == bestHour.getHour()) {
            return "You have selected an optimal time with balanced demand and pricing.";
        }

        if (bestHour.getRealDemand() < currentHour.getRealDemand() && bestHour.getPrice() < currentHour.getPrice()) {
            return "You selected " + currentHour.getLabel() + ". A better option is " + bestHour.getLabel()
                    + " where crowd is lower and pricing is more affordable.";
        }

        if (bestHour.getRealDemand() < currentHour.getRealDemand()) {
            return "You selected " + currentHour.getLabel() + ". Consider " + bestHour.getLabel()
                    + " for a less crowded experience.";
        }

        if (bestHour.getPrice() < currentHour.getPrice()) {
            return "You selected " + currentHour.getLabel() + ". You can save more by booking at " + bestHour.getLabel() + ".";
        }

        if ("High".equals(demandLevel)) {
            return "Peak hours detected around " + currentHour.getLabel() + ". Nearby slots can reduce wait time and cost.";
        }

        return "You may explore nearby time slots around " + bestHour.getLabel() + " for a better experience.";
    }

    private String formatHour(int hour) {
        int normalized = ((hour % 24) + 24) % 24;
        int displayHour = normalized % 12 == 0 ? 12 : normalized % 12;
        String meridiem = normalized < 12 ? "AM" : "PM";
        return displayHour + " " + meridiem;
    }

    private double round(Double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
