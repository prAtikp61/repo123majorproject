package com.Major.majorProject.repository;
import com.Major.majorProject.entity.Slot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query; // Add this import
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface SlotRepository extends JpaRepository<Slot, Long> {
    List<Slot> findByPcId(Long pcId);

    // New method to count total slots for a specific PC
    long countByPcId(Long pcId);

    // New method to count total slots for all PCs in a cafe
    @Query("SELECT COUNT(s) FROM Slot s WHERE s.pc.cafe.id = :cafeId")
    long countSlotsByCafeId(@Param("cafeId") Long cafeId);
}