package com.Major.majorProject.repository;

import com.Major.majorProject.entity.OfflineBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface OfflineBookingRepository extends JpaRepository<OfflineBooking, Long> {
    @Query("SELECT COALESCE(SUM(ob.customerCount), 0) FROM OfflineBooking ob WHERE ob.cafe.id = :cafeId AND ob.bookingDate = :bookingDate AND ob.timeSlot = :timeSlot")
    Long sumCustomerCount(@Param("cafeId") Long cafeId,
                          @Param("bookingDate") LocalDate bookingDate,
                          @Param("timeSlot") Integer timeSlot);

    List<OfflineBooking> findTop10ByCafeIdOrderByBookingDateDescTimeSlotDesc(Long cafeId);
}
