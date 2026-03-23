package com.Major.majorProject.service;

import com.Major.majorProject.entity.Slot; // Import Slot
import com.Major.majorProject.entity.UserBooking;
import com.Major.majorProject.repository.SlotRepository; // Import SlotRepository
import com.Major.majorProject.repository.UserBookingRepository;
import jakarta.persistence.EntityNotFoundException; // Import this
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class BookingService {

    private final UserBookingRepository userBookingRepository;
    private final SlotRepository slotRepository; // Inject SlotRepository
    private static final int HOLD_DURATION_MINUTES = 10;

    @Autowired
    public BookingService(UserBookingRepository userBookingRepository, SlotRepository slotRepository) {
        this.userBookingRepository = userBookingRepository;
        this.slotRepository = slotRepository; // Initialize it
    }

    @Transactional
    public Long createBookingHold(Long slotId, LocalDate bookingDate) {
        if (bookingDate.isBefore(LocalDate.now())) {
            throw new IllegalStateException("You cannot book a past date.");
        }

        userBookingRepository.findBySlotIdAndBookingDateAndStatusAndExpirationTimeAfter(
            slotId, bookingDate, UserBooking.BookingStatus.PENDING, LocalDateTime.now()
        ).ifPresent(hold -> {
            throw new IllegalStateException("Slot is currently being booked by another user.");
        });

        if (userBookingRepository.existsBySlotIdAndBookingDateAndStatus(
                slotId, bookingDate, UserBooking.BookingStatus.BOOKED)) {
            throw new IllegalStateException("This slot is already booked for the selected date.");
        }

        // FIRST, fetch the full Slot object from the database.
        Slot slotToBook = slotRepository.findById(slotId)
                .orElseThrow(() -> new EntityNotFoundException("Slot not found with ID: " + slotId));

        UserBooking newHold = new UserBooking();

        // FIX 1: Pass the entire 'slotToBook' object to setSlot()
        newHold.setSlot(slotToBook);

        // FIX 2: Pass the enum constant to setStatus()
        newHold.setStatus(UserBooking.BookingStatus.PENDING);
        
        newHold.setExpirationTime(LocalDateTime.now().plusMinutes(HOLD_DURATION_MINUTES));
        
        // Populate old fields for backward compatibility
        newHold.setPc(slotToBook.getPc());
        newHold.setStartTime(slotToBook.getStartTime());
        newHold.setEndTime(slotToBook.getEndTime());
        newHold.setBookingDate(bookingDate);

        UserBooking savedHold = userBookingRepository.save(newHold);

        return savedHold.getId();
    }
}
