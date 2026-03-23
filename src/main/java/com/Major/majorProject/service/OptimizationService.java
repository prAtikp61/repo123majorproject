package com.Major.majorProject.service;

import com.Major.majorProject.dto.OptimizeResponseDto;
import com.Major.majorProject.entity.PricingRule;
import com.Major.majorProject.entity.UserBooking;
import com.Major.majorProject.repository.UserBookingRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OptimizationService {

    private final MlPredictionService mlPredictionService;
    private final PricingRuleService pricingRuleService;
    private final OfflineBookingService offlineBookingService;
    private final UserBookingRepository userBookingRepository;

    public OptimizationService(MlPredictionService mlPredictionService,
                               PricingRuleService pricingRuleService,
                               OfflineBookingService offlineBookingService,
                               UserBookingRepository userBookingRepository) {
        this.mlPredictionService = mlPredictionService;
        this.pricingRuleService = pricingRuleService;
        this.offlineBookingService = offlineBookingService;
        this.userBookingRepository = userBookingRepository;
    }

    public OptimizeResponseDto optimize(Long cafeId, int timeSlot, Integer dayOfWeek, LocalDate bookingDate) {
        LocalDate targetDate = bookingDate != null ? bookingDate : LocalDate.now();
        int targetDay = dayOfWeek != null ? dayOfWeek : targetDate.getDayOfWeek().getValue();

        int predictedDemand = mlPredictionService.predictDemand(timeSlot, targetDay);
        int onlineBookings = getOnlineDemand(cafeId, targetDate, timeSlot);
        int offlineBookings = offlineBookingService.getOfflineDemand(cafeId, targetDate, timeSlot);
        int realDemand = predictedDemand + onlineBookings + offlineBookings;

        PricingRule rule = pricingRuleService.getOrCreateByCafeId(cafeId);

        String demandLevel;
        int suggestedDuration;
        double price;
        String message;

        if (realDemand < rule.getLowDemandThreshold()) {
            demandLevel = "Low";
            suggestedDuration = 3;
            price = multiply(rule.getBasePrice(), rule.getLowMultiplier());
            message = "Low demand expected. Offer longer sessions to improve utilization.";
        } else if (realDemand > rule.getHighDemandThreshold()) {
            demandLevel = "High";
            suggestedDuration = 1;
            price = multiply(rule.getBasePrice(), rule.getHighMultiplier());
            message = "High demand expected. Shorter sessions help serve more players.";
        } else {
            demandLevel = "Moderate";
            suggestedDuration = 2;
            price = round(rule.getBasePrice());
            message = "Moderate demand expected. Keep the standard session duration.";
        }

        OptimizeResponseDto response = new OptimizeResponseDto();
        response.setPredictedDemand(predictedDemand);
        response.setOnlineBookings(onlineBookings);
        response.setOfflineBookings(offlineBookings);
        response.setRealDemand(realDemand);
        response.setDemandLevel(demandLevel);
        response.setPrice(price);
        response.setMessage(message);
        response.setSuggestedDuration(suggestedDuration);
        return response;
    }

    private int getOnlineDemand(Long cafeId, LocalDate bookingDate, int timeSlot) {
        List<UserBooking> bookings = userBookingRepository.findByPcCafeIdAndBookingDateAndStatusIn(
                cafeId,
                bookingDate,
                List.of(UserBooking.BookingStatus.BOOKED, UserBooking.BookingStatus.PENDING)
        );

        return (int) bookings.stream()
                .filter(booking -> booking.getStartTime() != null && booking.getStartTime().getHour() == timeSlot)
                .filter(booking -> booking.getStatus() == UserBooking.BookingStatus.BOOKED
                        || (booking.getStatus() == UserBooking.BookingStatus.PENDING
                        && booking.getExpirationTime() != null
                        && booking.getExpirationTime().isAfter(LocalDateTime.now())))
                .count();
    }

    private double multiply(Double value, Double multiplier) {
        return round(value * multiplier);
    }

    private double round(Double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
