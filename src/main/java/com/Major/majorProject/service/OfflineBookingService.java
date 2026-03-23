package com.Major.majorProject.service;

import com.Major.majorProject.dto.OfflineBookingDto;
import com.Major.majorProject.entity.Cafe;
import com.Major.majorProject.entity.CafeOwner;
import com.Major.majorProject.entity.OfflineBooking;
import com.Major.majorProject.repository.CafeOwnerRepository;
import com.Major.majorProject.repository.CafeRepository;
import com.Major.majorProject.repository.OfflineBookingRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class OfflineBookingService {

    private final OfflineBookingRepository offlineBookingRepository;
    private final CafeRepository cafeRepository;
    private final CafeOwnerRepository cafeOwnerRepository;

    public OfflineBookingService(OfflineBookingRepository offlineBookingRepository,
                                 CafeRepository cafeRepository,
                                 CafeOwnerRepository cafeOwnerRepository) {
        this.offlineBookingRepository = offlineBookingRepository;
        this.cafeRepository = cafeRepository;
        this.cafeOwnerRepository = cafeOwnerRepository;
    }

    @Transactional
    public void addOfflineBooking(Long cafeId, OfflineBookingDto dto) {
        Cafe cafe = getOwnedCafe(cafeId);
        OfflineBooking offlineBooking = new OfflineBooking();
        offlineBooking.setCafe(cafe);
        offlineBooking.setBookingDate(dto.getBookingDate());
        offlineBooking.setTimeSlot(dto.getTimeSlot());
        offlineBooking.setCustomerCount(dto.getCustomerCount());
        offlineBooking.setNotes(dto.getNotes());
        offlineBookingRepository.save(offlineBooking);
    }

    public int getOfflineDemand(Long cafeId, LocalDate bookingDate, Integer timeSlot) {
        return Math.toIntExact(offlineBookingRepository.sumCustomerCount(cafeId, bookingDate, timeSlot));
    }

    public List<OfflineBooking> getRecentBookings(Long cafeId) {
        getOwnedCafe(cafeId);
        return offlineBookingRepository.findTop10ByCafeIdOrderByBookingDateDescTimeSlotDesc(cafeId);
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
