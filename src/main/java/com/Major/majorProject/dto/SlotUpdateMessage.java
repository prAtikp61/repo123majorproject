package com.Major.majorProject.dto;

public class SlotUpdateMessage {
    private Long slotId;
    private boolean isBooked;

    public SlotUpdateMessage(Long slotId, boolean isBooked) {
        this.slotId = slotId;
        this.isBooked = isBooked;
    }

    // Getters and Setters
    public Long getSlotId() {
        return slotId;
    }

    public void setSlotId(Long slotId) {
        this.slotId = slotId;
    }

    public boolean isBooked() {
        return isBooked;
    }

    public void setBooked(boolean booked) {
        isBooked = booked;
    }
}