// package com.Major.majorProject.controller;

// import com.Major.majorProject.service.BookingService;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;

// import java.util.Map;

// // This can be a @RestController
// @RestController
// @RequestMapping("/api/bookings")
// public class BookingController {

//     private final BookingService bookingService;

//     @Autowired
//     public BookingController(BookingService bookingService) {
//         this.bookingService = bookingService;
//     }

//     @PostMapping("/hold")
//     public ResponseEntity<?> createBookingHold(@RequestBody Map<String, Long> payload) {
//         try {
//             Long slotId = payload.get("slotId");
//             Long bookingId = bookingService.createBookingHold(slotId);
//             // Success: return the new bookingId
//             return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("bookingId", bookingId));
//         } catch (IllegalStateException e) {
//             // Failure (slot taken): return a conflict error
//             return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
//         }
//     }
// }

// package com.Major.majorProject.controller;

// import com.Major.majorProject.entity.Slot;
// import com.Major.majorProject.repository.SlotRepository;
// import com.Major.majorProject.service.BookingService;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Controller;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.RequestParam;
// import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// @Controller // Use @Controller for web page actions and redirects
// public class BookingController {

//     private final BookingService bookingService;
//     private final SlotRepository slotRepository;

//     @Autowired
//     public BookingController(BookingService bookingService, SlotRepository slotRepository) {
//         this.bookingService = bookingService;
//         this.slotRepository = slotRepository;
//     }

//     /**
//      * Handles the form submission when a user clicks the "Book" button on a slot.
//      * This is the endpoint your form's "action" should point to.
//      */
//     @PostMapping("/bookings/create-hold")
//     public String createBookingHoldFromForm(@RequestParam("slotId") Long slotId, RedirectAttributes redirectAttributes) {
//         try {
//             // Call the service to create the 10-minute hold on the slot
//             Long bookingId = bookingService.createBookingHold(slotId);
            
//             // SUCCESS: Redirect the user's browser to the confirmation page.
//             // You will need to create a controller and page for this URL in the next step.
//             return "redirect:/bookings/confirm/" + bookingId;

//         } catch (IllegalStateException e) {
//             // FAILURE (e.g., slot is already taken): Redirect the user BACK to the slots page with an error message.
            
//             // We need to find the PC ID from the slot to build the correct redirect URL.
//             // This ensures the user is sent back to the exact page they were on.
//             Slot slot = slotRepository.findById(slotId).orElse(null);
//             Long pcId = (slot != null) ? slot.getPc().getId() : 0; // Fallback to 0 if slot not found

//             // Add the error message as a "flash attribute" which survives the redirect.
//             redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            
//             // Redirect back to the URL that is handled by your SlotController.
//             return "redirect:/allSlots/pc/" + pcId;
//         }
//     }
// }


package com.Major.majorProject.controller;

import com.Major.majorProject.entity.Slot;
import com.Major.majorProject.entity.UserBooking;
import com.Major.majorProject.repository.SlotRepository;
import com.Major.majorProject.repository.UserBookingRepository;
import com.Major.majorProject.service.BookingService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Controller
public class BookingController {

    private final BookingService bookingService;
    private final SlotRepository slotRepository;
    private final UserBookingRepository userBookingRepository;

    @Autowired
    public BookingController(BookingService bookingService, SlotRepository slotRepository, UserBookingRepository userBookingRepository) {
        this.bookingService = bookingService;
        this.slotRepository = slotRepository;
        this.userBookingRepository = userBookingRepository;
    }

    /**
     * Handles the form submission when a user clicks the "Book" button.
     * It creates the hold and then REDIRECTS to the confirmation page.
     */
    @PostMapping("/bookings/create-hold")
    public String createBookingHoldFromForm(@RequestParam("slotId") Long slotId,
                                            @RequestParam("bookingDate") LocalDate bookingDate,
                                            RedirectAttributes redirectAttributes) {
        try {
            Long bookingId = bookingService.createBookingHold(slotId, bookingDate);
            
            // SUCCESS: This is the redirect action. It tells the browser to go to a new URL.
            return "redirect:/bookings/confirm/" + bookingId;

        } catch (IllegalStateException e) {
            // FAILURE: Redirects back to the slot list with an error message.
            Slot slot = slotRepository.findById(slotId).orElse(null);
            Long pcId = (slot != null) ? slot.getPc().getId() : 0;
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/allSlots/pc/" + pcId + "?date=" + bookingDate;
        }
    }

    /**
     * This method CATCHES the redirect from the method above.
     * It displays the userBookingConfirm.html page.
     */
    @GetMapping("/bookings/confirm/{bookingId}")
    public String showConfirmationPage(@PathVariable Long bookingId, Model model) {
        UserBooking booking = userBookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found. It may have expired."));
        
        if (booking.getExpirationTime().isBefore(LocalDateTime.now())) {
            return "user/bookingExpired"; // Show an expired page if the hold is old
        }
        
        model.addAttribute("booking", booking);
        return "user/userBookingConfirm"; // Correctly renders your confirmation page
    }
}
