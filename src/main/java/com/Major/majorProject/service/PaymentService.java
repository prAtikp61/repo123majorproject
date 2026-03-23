package com.Major.majorProject.service;

import com.Major.majorProject.dto.SlotUpdateMessage;
import com.Major.majorProject.entity.User;
import com.Major.majorProject.entity.UserBooking;
import com.Major.majorProject.repository.UserBookingRepository;
import com.Major.majorProject.repository.UserRepository;
import com.itextpdf.text.Document;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import jakarta.mail.internet.MimeMessage;

import java.io.ByteArrayOutputStream;
import org.springframework.core.io.ByteArrayResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.Major.majorProject.entity.Slot;

@Service
public class PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);

    @Value("${stripe.secret.key:}")
    private String stripeSecretKey;

    private final JavaMailSender mailSender;


    private final UserBookingRepository userBookingRepository;
    private final UserRepository userRepository;
        private final SimpMessagingTemplate messagingTemplate; // Add this field


    public PaymentService(UserBookingRepository userBookingRepository, 
                          UserRepository userRepository,
                          SimpMessagingTemplate messagingTemplate,
                          JavaMailSender mailSender) { // Add this parameter
        this.userBookingRepository = userBookingRepository;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
        this.mailSender = mailSender;
    }
    
    @PostConstruct
    public void init() {
        if (stripeSecretKey != null && !stripeSecretKey.isBlank()) {
            Stripe.apiKey = stripeSecretKey;
        }
    }

    @Transactional
    public String createStripeCheckoutSession(Long bookingId, String customerName, String customerEmail, String customerPhone) throws StripeException {
        logger.info("Starting a simple checkout process for bookingId: {}", bookingId);
        
        UserBooking booking = attachUserToBooking(bookingId, customerName, customerEmail, customerPhone);
        
        long priceInCents = booking.getSlot().getPc().getCafe().getHourlyRate().longValue() * 100;
        String successUrl = "http://localhost:8080/payment/success?session_id={CHECKOUT_SESSION_ID}";
        String cancelUrl = "http://localhost:8080/payment/cancel";

        SessionCreateParams params = SessionCreateParams.builder()
                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(successUrl)
                .setCancelUrl(cancelUrl)
                .addLineItem(
                    SessionCreateParams.LineItem.builder()
                        .setQuantity(1L)
                        .setPriceData(
                                SessionCreateParams.LineItem.PriceData.builder() 
                                .setCurrency("INR") // Change to "inr" if your platform is in India
                                .setUnitAmount(priceInCents)
                                .setProductData(
                                    SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                        .setName("Gaming Slot at " + booking.getSlot().getPc().getCafe().getName())
                                        .build())
                                .build())
                        .build())
                // The setPaymentIntentData block for transferring funds is now removed
                .putMetadata("bookingId", bookingId.toString())
                .build();
        
        Session session = Session.create(params);
        return session.getUrl();
    }

    /**
     * Confirms a payment, updates database records, and broadcasts the slot status.
     * This method is transactional, ensuring all DB operations succeed or fail together.
     * @return 
     */
    @Transactional
    public Long confirmPayment(String sessionId) throws StripeException {
        Session session = Session.retrieve(sessionId);
        String paymentStatus = session.getPaymentStatus();
        

        if ("paid".equals(paymentStatus)) {
            // PAYMENT IS COMPLETE
            logger.info("Payment successful for session_id: {}", sessionId);

            String bookingIdStr = session.getMetadata().get("bookingId");
            Long bookingId = Long.parseLong(bookingIdStr);

            // 1. Find the booking and update its status
            UserBooking booking = userBookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalStateException("Booking not found with ID: " + bookingId));
            
            // Check if booking was already processed to prevent duplicate updates
            if (booking.getStatus() == UserBooking.BookingStatus.PENDING) {
                booking.setStatus(UserBooking.BookingStatus.BOOKED);
                userBookingRepository.save(booking);
                Slot slot = booking.getSlot();
                
                logger.info("Booking ID {} status updated to BOOKED and Slot ID {} marked as booked.", booking.getId(), slot.getId());
                
                SlotUpdateMessage message = new SlotUpdateMessage(slot.getId(), true);
                messagingTemplate.convertAndSend("/topic/slot-updates", message);
                logger.info("Broadcasted update for Slot ID {}", slot.getId());
            } else {
                 logger.warn("Booking ID {} was already processed. Status: {}", booking.getId(), booking.getStatus());
            }
            return bookingId;

        } else {
            logger.warn("Payment for session {} not completed. Status: {}", sessionId, paymentStatus);
            throw new IllegalStateException("Payment status is not 'paid'.");
        }
    }

    /**
     * Retrieves the Stripe session. This is needed to get the bookingId from metadata.
     */
    public Session retrieveStripeSession(String sessionId) throws StripeException {
        return Session.retrieve(sessionId);
    }

    @Transactional
    public Long bypassPayment(Long bookingId, String customerName, String customerEmail, String customerPhone) {
        UserBooking booking = attachUserToBooking(bookingId, customerName, customerEmail, customerPhone);

        if (booking.getStatus() == UserBooking.BookingStatus.PENDING) {
            booking.setStatus(UserBooking.BookingStatus.BOOKED);
            userBookingRepository.save(booking);
            messagingTemplate.convertAndSend("/topic/slot-updates", new SlotUpdateMessage(booking.getSlot().getId(), true));
        }

        return booking.getId();
    }

    private UserBooking attachUserToBooking(Long bookingId, String customerName, String customerEmail, String customerPhone) {
        UserBooking booking = userBookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found with ID: " + bookingId));

        User user = userRepository.findByEmail(customerEmail)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setName(customerName);
                    newUser.setEmail(customerEmail);
                    if (customerPhone != null && !customerPhone.isEmpty()) {
                        newUser.setPhone(customerPhone);
                    }
                    return userRepository.save(newUser);
                });
        booking.setUser(user);
        return userBookingRepository.save(booking);
    }

    /**
     * Generates a PDF confirmation and emails it to the user.
     * This method is marked @Async to prevent blocking the main request thread.
     */
    @Async
    public void generateAndEmailConfirmation(Long bookingId) {
        try {
            UserBooking booking = userBookingRepository.findById(bookingId)
                    .orElseThrow(() -> new IllegalArgumentException("Booking not found for PDF generation: " + bookingId));

            // Step 1: Generate PDF
            ByteArrayOutputStream pdfOutputStream = new ByteArrayOutputStream();
            Document document = new Document();
            PdfWriter.getInstance(document, pdfOutputStream);
            document.open();

            document.add(new Paragraph("Booking Confirmation", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18)));
            document.add(new Paragraph("------------------------------------"));
            document.add(new Paragraph("Cafe: " + booking.getSlot().getPc().getCafe().getName()));
            // document.add(new Paragraph("PC/Seat: " + booking.getSlot().getPc().getSeatName()));
            document.add(new Paragraph("Slot Time: " + booking.getSlot().getStartTime() + " to " + booking.getSlot().getEndTime()));
            document.add(new Paragraph("Booking ID: " + booking.getId()));
            document.add(new Paragraph("Amount Paid: INR " + booking.getSlot().getPc().getCafe().getHourlyRate()));
            document.close();
            byte[] pdfBytes = pdfOutputStream.toByteArray();

            // Step 2: Send Email
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom("your_email@example.com"); // Set your sender email
            helper.setTo(booking.getUser().getEmail());
            helper.setSubject("Your Booking Confirmation from " + booking.getSlot().getPc().getCafe().getName());
            helper.setText("Dear " + booking.getUser().getName() + ",\n\n" +
                    "Thank you for your booking! Your confirmation is attached. We look forward to seeing you.\n\n" +
                    "Regards,\nThe " + booking.getSlot().getPc().getCafe().getName() + " Team", false);

            helper.addAttachment("booking-confirmation.pdf", new ByteArrayResource(pdfBytes));

            mailSender.send(message);
            logger.info("Booking confirmation email sent successfully to {}", booking.getUser().getEmail());

        } catch (Exception e) {
            logger.error("Failed to generate or send booking confirmation email for bookingId: " + bookingId, e);
        }
    }
}

