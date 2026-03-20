package com.Major.majorProject.service;

import com.Major.majorProject.dto.*;
import com.Major.majorProject.entity.Cafe;
import com.Major.majorProject.entity.PC;
import com.Major.majorProject.entity.UserBooking; // Import for enum
import com.Major.majorProject.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    private final UserGamePreferenceRepository userGamePreferenceRepository;
    private final UserBookingRepository userBookingRepository; // Inject new repo
    private final SlotRepository slotRepository; // Inject SlotRepository
    private final PCRepository pcRepository;
    private final CafeRepository cafeRepository;

    @Autowired
    public AnalyticsService(UserGamePreferenceRepository userGamePreferenceRepository,
                            UserBookingRepository userBookingRepository,SlotRepository srepo,PCRepository pcrepo,CafeRepository caferepo) { // Add to constructor
        this.userGamePreferenceRepository = userGamePreferenceRepository;
        this.userBookingRepository = userBookingRepository; // Initialize it
        this.pcRepository =pcrepo;
        this.slotRepository=srepo;
        this.cafeRepository = caferepo;
    }

    public List<GameCountDto> getMostPreferredGamesForCafe(Long cafeId) {
        // Business logic goes here (currently just a repository call)
        return userGamePreferenceRepository.findMostPreferredGamesByCafeId(cafeId);
    }

    // New method for monthly bookings
    public List<MonthlyBookingDto> getMonthlyBookingPatternForCafe(Long cafeId) {
        // Pass the cafeId and the 'BOOKED' status to the repository query
        return userBookingRepository.findMonthlyBookingCountsByCafe(cafeId, UserBooking.BookingStatus.BOOKED);
    }

    public List<HourlyBookingDto> getHourlyBookingPatternForCafe(Long cafeId) {
        return userBookingRepository.findHourlyBookingCountsByCafe(cafeId, UserBooking.BookingStatus.BOOKED);
    }

    public List<PcUtilizationDto> getPcUtilizationForCafe(Long cafeId) {
        // 1. Get all PCs for the cafe
        List<PC> pcsInCafe = pcRepository.findByCafeId(cafeId);
        if (pcsInCafe.isEmpty()) {
            return new ArrayList<>();
        }

        // 2. Get booked counts per PC seat number
        List<PcBookingCountDto> bookedCountsList = userBookingRepository.countBookedSlotsPerPcByCafe(cafeId, UserBooking.BookingStatus.BOOKED);
        // Convert list to a Map for easy lookup: Map<SeatNumber, BookedCount>
        Map<Integer, Long> bookedCountsMap = bookedCountsList.stream()
                .collect(Collectors.toMap(PcBookingCountDto::getSeatNumber, PcBookingCountDto::getCount));

        // 3. Calculate utilization for each PC
        List<PcUtilizationDto> utilizationList = new ArrayList<>();
        for (PC pc : pcsInCafe) {
            long totalSlotsForPc = slotRepository.countByPcId(pc.getId()); // Get total slots for this specific PC
            long bookedSlotsForPc = bookedCountsMap.getOrDefault(pc.getSeatNumber(), 0L); // Get booked count, default to 0

            PcUtilizationDto dto = new PcUtilizationDto(pc.getSeatNumber(), bookedSlotsForPc, totalSlotsForPc);
            utilizationList.add(dto);
        }

        // Optional: Sort by seat number
        utilizationList.sort(Comparator.comparingInt(PcUtilizationDto::getSeatNumber));

        return utilizationList;
    }

    public List<PcRevenueDto> getPcRevenueForCafe(Long cafeId) {
        // 1. Get the cafe to find its hourly rate
        Cafe cafe = cafeRepository.findById(cafeId)
                .orElseThrow(() -> new EntityNotFoundException("Cafe not found with ID: " + cafeId));
        double hourlyRate = cafe.getHourlyRate() != null ? cafe.getHourlyRate() : 0.0;

        // 2. Get booked counts per PC seat number (reuse query from Utilization)
        List<PcBookingCountDto> bookedCountsList = userBookingRepository.countBookedSlotsPerPcByCafe(cafeId, UserBooking.BookingStatus.BOOKED);

        // 3. Calculate revenue for each PC that has bookings
        List<PcRevenueDto> revenueList = bookedCountsList.stream()
                .map(bookedCount -> new PcRevenueDto(
                        bookedCount.getSeatNumber(),
                        bookedCount.getCount(),
                        hourlyRate
                ))
                .collect(Collectors.toList());

        // Optional: Ensure all PCs from the cafe are included, even if they have 0 revenue
        List<PC> allPcsInCafe = pcRepository.findByCafeId(cafeId);
        Map<Integer, PcRevenueDto> revenueMap = revenueList.stream()
                .collect(Collectors.toMap(PcRevenueDto::getSeatNumber, dto -> dto));

        List<PcRevenueDto> fullRevenueList = allPcsInCafe.stream()
                .map(pc -> revenueMap.getOrDefault(
                        pc.getSeatNumber(),
                        new PcRevenueDto(pc.getSeatNumber(), 0L, hourlyRate) // Default to 0 revenue if no bookings
                ))
                .sorted(Comparator.comparingInt(PcRevenueDto::getSeatNumber)) // Sort by seat number
                .collect(Collectors.toList());


        return fullRevenueList; // Return the list including PCs with 0 revenue
    }
}