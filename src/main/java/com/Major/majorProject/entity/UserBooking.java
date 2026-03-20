package com.Major.majorProject.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
public class UserBooking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDate bookingDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private LocalDateTime expirationTime; // From java.time.LocalDateTime

    @ManyToOne
    @JoinColumn(name = "pc_id", nullable = false)
    private PC pc;

    @ManyToOne
    @JoinColumn(name = "slot_id", nullable = false)
    private Slot slot; // Link directly to the slot

    // to this
    @Enumerated(EnumType.STRING)
    @Column(length = 20) // Defines the column size for the database
    private BookingStatus status = BookingStatus.PENDING; // Default to PENDING

    public enum BookingStatus {
        PENDING, // A temporary hold, not yet paid
        BOOKED,  // Paid and confirmed
        COMPLETED,
        CANCELLED
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(LocalDate bookingDate) {
        this.bookingDate = bookingDate;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public LocalDateTime getExpirationTime() {
        return expirationTime;
    }

    public void setExpirationTime(LocalDateTime expirationTime) {
        this.expirationTime = expirationTime;
    }

    public PC getPc() {
        return pc;
    }

    public void setPc(PC pc) {
        this.pc = pc;
    }

    public Slot getSlot() {
        return slot;
    }

    public void setSlot(Slot slot) {
        this.slot = slot;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}



// changed this
    // @Enumerated(EnumType.STRING)
    // private BookingStatus status = BookingStatus.BOOKED;

    // public enum BookingStatus {
    //     BOOKED, COMPLETED, CANCELLED
    // }