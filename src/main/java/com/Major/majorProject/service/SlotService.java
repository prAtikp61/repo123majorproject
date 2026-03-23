package com.Major.majorProject.service;

import com.Major.majorProject.dto.SlotDto;
import com.Major.majorProject.entity.Slot;
import com.Major.majorProject.entity.UserBooking;
import com.Major.majorProject.repository.SlotRepository;
import com.Major.majorProject.repository.UserBookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SlotService {

    private final SlotRepository slotRepository;
    private final UserBookingRepository userBookingRepository;

    @Autowired
    public SlotService(SlotRepository slotRepository, UserBookingRepository userBookingRepository) {
        this.slotRepository = slotRepository;
        this.userBookingRepository = userBookingRepository;
    }

    // This method finds ALL slots
    public List<SlotDto> getAllSlots() {
        List<Slot> slots = slotRepository.findAll();
        return slots.stream()
                .map(slot -> convertToDto(slot, slot.isBooked()))
                .collect(Collectors.toList());
    }

    // This method finds slots for a specific PC
    public List<SlotDto> getSlotsByPcId(Long pcId) {
        return getSlotsByPcIdAndDate(pcId, LocalDate.now());
    }

    public List<SlotDto> getSlotsByPcIdAndDate(Long pcId, LocalDate bookingDate) {
        List<Slot> slotsFromDb = slotRepository.findByPcId(pcId);
        Set<Long> blockedSlotIds = userBookingRepository.findBySlotPcIdAndBookingDateAndStatusIn(
                        pcId,
                        bookingDate,
                        List.of(UserBooking.BookingStatus.BOOKED, UserBooking.BookingStatus.PENDING)
                ).stream()
                .filter(booking -> booking.getStatus() == UserBooking.BookingStatus.BOOKED
                        || (booking.getExpirationTime() != null && booking.getExpirationTime().isAfter(LocalDateTime.now())))
                .map(booking -> booking.getSlot().getId())
                .collect(Collectors.toSet());

        return slotsFromDb.stream()
                .map(slot -> convertToDto(slot, blockedSlotIds.contains(slot.getId())))
                .collect(Collectors.toList());
    }


    // This helper method is now corrected to match your SlotDto
    private SlotDto convertToDto(Slot slot, boolean blocked) {
        // The constructor arguments must match the order of fields in SlotDto:
        // 1. startTime (List<LocalTime>)
        // 2. id (Long)
        // 3. pcId (Long)
        // 4. endTime (LocalTime)
        // 5. isBooked (boolean)
        return new SlotDto(
                Collections.singletonList(slot.getStartTime()), // Wrapped the single time in a List
                slot.getId(),
                slot.getPc().getId(),
                slot.getEndTime(),
                blocked
        );
    }
}
