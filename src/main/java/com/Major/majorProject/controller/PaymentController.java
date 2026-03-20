// package com.Major.majorProject.controller;

// import com.Major.majorProject.service.PaymentService;
// import com.stripe.exception.StripeException;
// import com.stripe.model.checkout.Session;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Controller;
// import org.springframework.ui.Model;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.RequestParam;

// @Controller
// public class PaymentController {

//     private final PaymentService paymentService;

//     @Autowired
//     public PaymentController(PaymentService paymentService) {
//         this.paymentService = paymentService;
//     }

//      @GetMapping("/payment/success")
//     public String paymentSuccess(@RequestParam("session_id") String sessionId, Model model) {
//         try {
//             // First, retrieve the Stripe session to get the metadata
//             Session session = paymentService.retrieveStripeSession(sessionId);
//             String bookingIdStr = session.getMetadata().get("bookingId");
//             if (bookingIdStr == null) {
//                 throw new IllegalStateException("Booking ID not found in session metadata.");
//             }
//             Long bookingId = Long.parseLong(bookingIdStr);
//             // *** THIS IS THE CRITICAL LINE TO ADD ***
//             paymentService.confirmPayment(sessionId);

//             // Trigger the PDF generation and email sending (this method is @Async)
//             paymentService.generateAndEmailConfirmation(bookingId);

//             model.addAttribute("message", "Your payment was successful and the booking is confirmed!");
//             return "user/payment-success"; // Show the success page

//         } catch (StripeException | IllegalStateException e) {
//             // If confirmation fails (e.g., payment status wasn't 'paid' or booking not found)
//             e.printStackTrace(); // Log the error
//             model.addAttribute("error", "There was an issue confirming your payment: " + e.getMessage());
//             return "user/payment-error"; // Show an error page
//         }
//     }

//     @PostMapping("/payments/create-checkout")
//     public String createCheckoutSession(@RequestParam Long bookingId,
//                                         @RequestParam String customerName,
//                                         @RequestParam String customerEmail,
//                                         @RequestParam(required = false) String customerPhone) {
//         try {
//             String stripeUrl = paymentService.createStripeCheckoutSession(bookingId, customerName, customerEmail, customerPhone);
//             return "redirect:" + stripeUrl;
            
//         } catch (StripeException | IllegalStateException e) {
//             e.printStackTrace();
//             // In case of an error, you can redirect to a page that shows a helpful message
//             return "redirect:/payment-error";
//         }
//     }
// }

// Current Working Controller - Done By Me
// package com.Major.majorProject.controller;

// import com.Major.majorProject.service.PaymentService;
// import com.stripe.exception.StripeException;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Controller;
// import org.springframework.ui.Model;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.RequestParam;

// @Controller
// public class PaymentController {

//     private final PaymentService paymentService;

//     @Autowired
//     public PaymentController(PaymentService paymentService) {
//         this.paymentService = paymentService;
//     }

//      @GetMapping("/payment/success")
//     public String paymentSuccess(@RequestParam("session_id") String sessionId, Model model) {
//         try {
//             // Confirm payment and get the bookingId in a single step
//             Long bookingId = paymentService.confirmPayment(sessionId);
//             // *** THIS IS THE CRITICAL LINE TO ADD ***
//             // paymentService.confirmPayment(sessionId);

//             // Trigger the PDF generation and email sending (this method is @Async)
//             paymentService.generateAndEmailConfirmation(bookingId);

//             model.addAttribute("message", "Your payment was successful and the booking is confirmed!");
//             return "user/payment-success"; // Show the success page

//         } catch (StripeException | IllegalStateException e) {
//             // If confirmation fails (e.g., payment status wasn't 'paid' or booking not found)
//             e.printStackTrace(); // Log the error
//             model.addAttribute("error", "There was an issue confirming your payment: " + e.getMessage());
//             return "user/payment-error"; // Show an error page
//         }
//     }

//     @PostMapping("/payments/create-checkout")
//     public String createCheckoutSession(@RequestParam Long bookingId,
//                                         @RequestParam String customerName,
//                                         @RequestParam String customerEmail,
//                                         @RequestParam(required = false) String customerPhone) {
//         try {
//             String stripeUrl = paymentService.createStripeCheckoutSession(bookingId, customerName, customerEmail, customerPhone);
//             return "redirect:" + stripeUrl;
            
//         } catch (StripeException | IllegalStateException e) {
//             e.printStackTrace();
//             // In case of an error, you can redirect to a page that shows a helpful message
//             return "redirect:/payment-error";
//         }
//     }
// }
package com.Major.majorProject.controller;

import com.Major.majorProject.entity.Cafe;
import com.Major.majorProject.entity.User;
import com.Major.majorProject.entity.UserBooking;
import com.Major.majorProject.entity.UserGamePreference;
import com.Major.majorProject.repository.UserBookingRepository;
import com.Major.majorProject.repository.UserGamePreferenceRepository;
import com.Major.majorProject.repository.UserRepository;
import com.Major.majorProject.service.PaymentService;
import com.stripe.exception.StripeException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class PaymentController {

    private final PaymentService paymentService;
    private final UserBookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final UserGamePreferenceRepository userGamePreferenceRepository;

    @Autowired
    public PaymentController(PaymentService paymentService, UserBookingRepository bookingRepository, UserRepository userRepository, UserGamePreferenceRepository userGamePreferenceRepository) {
        this.paymentService = paymentService;
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.userGamePreferenceRepository = userGamePreferenceRepository;
    }

    @GetMapping("/payment/success")
    public String paymentSuccess(@RequestParam("session_id") String sessionId, Model model) {
        try {
            Long bookingId = paymentService.confirmPayment(sessionId);
            paymentService.generateAndEmailConfirmation(bookingId);
            model.addAttribute("message", "Your payment was successful and the booking is confirmed!");
            return "user/payment-success";
        } catch (StripeException | IllegalStateException e) {
            e.printStackTrace();
            model.addAttribute("error", "There was an issue confirming your payment: " + e.getMessage());
            return "user/payment-error";
        }
    }

    @PostMapping("/payments/create-checkout")
    public String createCheckoutSession(@RequestParam Long bookingId,
                                        @RequestParam String customerName,
                                        @RequestParam String customerEmail,
                                        @RequestParam(required = false) String customerPhone,
                                        @RequestParam(name = "preferredGames", required = false) String preferredGamesString) {
        try {
            UserBooking userBooking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new IllegalStateException("Booking not found with ID: " + bookingId));

            User user = userBooking.getUser();
            
            // This is the CRUCIAL part that fixes the bug.
            if (user == null) {
                Optional<User> optionalUser = userRepository.findByEmail(customerEmail);
                if (optionalUser.isPresent()) {
                    user = optionalUser.get(); // Link the existing user
                } else {
                    // This creates the user if they don't exist
                    user = new User();
                    user.setName(customerName);
                    user.setEmail(customerEmail);
                    user.setPhone(customerPhone);
                    userRepository.save(user); // Save the new user to the database
                }
                userBooking.setUser(user);
                bookingRepository.save(userBooking); // Link the new user to the booking
            }
            
            Cafe cafe = userBooking.getPc().getCafe();

            if (user != null && preferredGamesString != null && !preferredGamesString.isEmpty()) {
                List<String> preferredGames = Arrays.stream(preferredGamesString.split(","))
                        .map(String::trim)
                        .collect(Collectors.toList());

                for (String gameName : preferredGames) {
                    UserGamePreference preference = new UserGamePreference();
                    preference.setUser(user);
                    preference.setGameName(gameName);
                    preference.setCafe(cafe);
                    userGamePreferenceRepository.save(preference);
                }
            }

            String stripeUrl = paymentService.createStripeCheckoutSession(bookingId, customerName, customerEmail, customerPhone);
            return "redirect:" + stripeUrl;

        } catch (StripeException | IllegalStateException e) {
            e.printStackTrace();
            return "redirect:/payment-error";
        }
    }
}