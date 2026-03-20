package com.Major.majorProject.service;

import com.Major.majorProject.dto.SlotDto;
import com.Major.majorProject.entity.Slot;
import com.Major.majorProject.repository.SlotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SlotService {

    private final SlotRepository slotRepository;

    @Autowired
    public SlotService(SlotRepository slotRepository) {
        this.slotRepository = slotRepository;
    }

    // This method finds ALL slots
    public List<SlotDto> getAllSlots() {
        List<Slot> slots = slotRepository.findAll();
        return slots.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // This method finds slots for a specific PC
    public List<SlotDto> getSlotsByPcId(Long pcId) {
        List<Slot> slotsFromDb = slotRepository.findByPcId(pcId);
        return slotsFromDb.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }


    // This helper method is now corrected to match your SlotDto
    private SlotDto convertToDto(Slot slot) {
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
                slot.isBooked()
        );
    }
}