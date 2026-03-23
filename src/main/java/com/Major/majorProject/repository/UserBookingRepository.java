package com.Major.majorProject.repository;

import com.Major.majorProject.dto.HourlyBookingDto;
import com.Major.majorProject.dto.MonthlyBookingDto;
import com.Major.majorProject.dto.PcBookingCountDto;
import com.Major.majorProject.entity.PC;
import com.Major.majorProject.entity.UserBooking;
import com.Major.majorProject.entity.UserBooking.BookingStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserBookingRepository extends JpaRepository<UserBooking, Long> {
    List<UserBooking> findByPcIdAndBookingDate(Long pcId, LocalDate bookingDate);

    Optional<UserBooking> findBySlotIdAndStatusAndExpirationTimeAfter(
            Long slotId, BookingStatus pending, LocalDateTime currentTime
    );

    Optional<UserBooking> findBySlotIdAndBookingDateAndStatusAndExpirationTimeAfter(
            Long slotId, LocalDate bookingDate, BookingStatus pending, LocalDateTime currentTime
    );

    boolean existsBySlotId(Long slotId);

    boolean existsBySlotIdAndBookingDateAndStatus(Long slotId, LocalDate bookingDate, BookingStatus status);

    boolean existsByPcCafeId(Long cafeId);

    List<UserBooking> findByPcCafeIdAndBookingDateAndStatusIn(Long cafeId, LocalDate bookingDate, List<BookingStatus> statuses);

    List<UserBooking> findBySlotPcIdAndBookingDateAndStatusIn(Long pcId, LocalDate bookingDate, List<BookingStatus> statuses);

    @Query("SELECT b FROM UserBooking b WHERE b.slot.id IN :slotIds AND (b.status = :bookedStatus OR (b.status = :pendingStatus AND b.expirationTime > :currentTime))")
    List<UserBooking> findActiveBookingsForSlots(
            @Param("slotIds") List<Long> slotIds,
            @Param("bookedStatus") UserBooking.BookingStatus bookedStatus,
            @Param("pendingStatus") UserBooking.BookingStatus pendingStatus,
            @Param("currentTime") LocalDateTime currentTime
    );

    @Query("SELECT new com.Major.majorProject.dto.MonthlyBookingDto(CAST(FUNCTION('MONTH', b.bookingDate) AS java.lang.Integer), COUNT(b.id)) " +
            "FROM UserBooking b " +
            "WHERE b.slot.pc.cafe.id = :cafeId AND b.status = :bookedStatus " +
            "GROUP BY CAST(FUNCTION('MONTH', b.bookingDate) AS java.lang.Integer) " +
            "ORDER BY CAST(FUNCTION('MONTH', b.bookingDate) AS java.lang.Integer)")
    List<MonthlyBookingDto> findMonthlyBookingCountsByCafe(
            @Param("cafeId") Long cafeId,
            @Param("bookedStatus") UserBooking.BookingStatus bookedStatus
    );

    /**
     * CORRECTED Hourly Query:
     * Explicitly CAST the result of FUNCTION('HOUR', ...) to INTEGER.
     * Ensure the GROUP BY and ORDER BY clauses match the SELECT expression.
     */
    @Query("SELECT new com.Major.majorProject.dto.HourlyBookingDto(CAST(FUNCTION('HOUR', b.startTime) AS java.lang.Integer), COUNT(b.id)) " +
            "FROM UserBooking b " +
            "WHERE b.slot.pc.cafe.id = :cafeId AND b.status = :bookedStatus " +
            // --- CORRECTED GROUP BY ---
            "GROUP BY CAST(FUNCTION('HOUR', b.startTime) AS java.lang.Integer) " +
            // --- CORRECTED ORDER BY ---
            "ORDER BY CAST(FUNCTION('HOUR', b.startTime) AS java.lang.Integer)")
    List<HourlyBookingDto> findHourlyBookingCountsByCafe(
            @Param("cafeId") Long cafeId,
            @Param("bookedStatus") UserBooking.BookingStatus bookedStatus
    );

//    @Query("SELECT new com.Major.majorProject.dto.PcBookingCountDto(b.slot.pc.seatNumber, COUNT(b.id)) " +
//            "FROM UserBooking b " +
//            "WHERE b.slot.pc.cafe.id = :cafeId AND b.status = :bookedStatus " +
//            "GROUP BY b.slot.pc.seatNumber")
//    List<PcBookingCountDto> countBookedSlotsPerPcByCafe(
//            @Param("cafeId") Long cafeId,
//            @Param("bookedStatus") UserBooking.BookingStatus bookedStatus
//    );

    /**
     * Counts BOOKED slots grouped by PC seat number for a specific cafe.
     * Returns a simple DTO containing seat number and count.
     */
    @Query("SELECT new com.Major.majorProject.dto.PcBookingCountDto(b.slot.pc.seatNumber, COUNT(b.id)) " +
            "FROM UserBooking b " +
            "WHERE b.slot.pc.cafe.id = :cafeId AND b.status = :bookedStatus " +
            "GROUP BY b.slot.pc.seatNumber")
    List<PcBookingCountDto> countBookedSlotsPerPcByCafe(
            @Param("cafeId") Long cafeId,
            @Param("bookedStatus") UserBooking.BookingStatus bookedStatus
    );

}
