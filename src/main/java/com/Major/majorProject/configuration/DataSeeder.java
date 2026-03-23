package com.Major.majorProject.configuration;

import com.Major.majorProject.entity.Cafe;
import com.Major.majorProject.entity.CafeOwner;
import com.Major.majorProject.entity.Game;
import com.Major.majorProject.entity.OfflineBooking;
import com.Major.majorProject.entity.PC;
import com.Major.majorProject.entity.PricingRule;
import com.Major.majorProject.entity.Slot;
import com.Major.majorProject.entity.User;
import com.Major.majorProject.entity.UserBooking;
import com.Major.majorProject.entity.UserGamePreference;
import com.Major.majorProject.repository.CafeOwnerRepository;
import com.Major.majorProject.repository.CafeRepository;
import com.Major.majorProject.repository.GameRepository;
import com.Major.majorProject.repository.OfflineBookingRepository;
import com.Major.majorProject.repository.PCRepository;
import com.Major.majorProject.repository.PricingRuleRepository;
import com.Major.majorProject.repository.SlotRepository;
import com.Major.majorProject.repository.UserBookingRepository;
import com.Major.majorProject.repository.UserGamePreferenceRepository;
import com.Major.majorProject.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seedDemoData(
            CafeOwnerRepository cafeOwnerRepository,
            CafeRepository cafeRepository,
            PCRepository pcRepository,
            SlotRepository slotRepository,
            UserRepository userRepository,
            UserBookingRepository userBookingRepository,
            UserGamePreferenceRepository userGamePreferenceRepository,
            GameRepository gameRepository,
            PricingRuleRepository pricingRuleRepository,
            OfflineBookingRepository offlineBookingRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            if (gameRepository.count() == 0) {
                seedGames(gameRepository);
            }

            CafeOwner owner = cafeOwnerRepository.findByEmail("owner@example.com")
                    .orElseGet(() -> {
                        CafeOwner newOwner = new CafeOwner();
                        newOwner.setName("Demo Owner");
                        newOwner.setEmail("owner@example.com");
                        newOwner.setPhone("9876543210");
                        newOwner.setPassword(passwordEncoder.encode("owner123"));
                        newOwner.setStripeAccountId("demo_stripe_account");
                        newOwner.setRazorpayAccountId("demo_razorpay_account");
                        return cafeOwnerRepository.save(newOwner);
                    });

            User userOne = findOrCreateUser(userRepository, "Aarav Patil", "aarav@example.com", "9999990001");
            User userTwo = findOrCreateUser(userRepository, "Riya Shah", "riya@example.com", "9999990002");
            User userThree = findOrCreateUser(userRepository, "Kabir Mehta", "kabir@example.com", "9999990003");
            User userFour = findOrCreateUser(userRepository, "Sneha Kulkarni", "sneha@example.com", "9999990004");
            User userFive = findOrCreateUser(userRepository, "Yash Deshmukh", "yash@example.com", "9999990005");
            User userSix = findOrCreateUser(userRepository, "Isha Verma", "isha@example.com", "9999990006");
            User userSeven = findOrCreateUser(userRepository, "Manav Joshi", "manav@example.com", "9999990007");
            User userEight = findOrCreateUser(userRepository, "Pooja Nair", "pooja@example.com", "9999990008");

            Cafe arena = findOrCreateCafe(cafeRepository, owner, "Arena PS Lounge", "FC Road, Pune", LocalTime.of(10, 0), LocalTime.of(22, 0), 120.0, "/images/cafe1.jpeg");
            Cafe midnight = findOrCreateCafe(cafeRepository, owner, "Midnight Console Hub", "Baner, Pune", LocalTime.of(11, 0), LocalTime.of(23, 0), 150.0, "/images/cafe2.jpg");

            seedPricingRule(pricingRuleRepository, arena, 120.0, 10, 25, 0.85, 1.40);
            seedPricingRule(pricingRuleRepository, midnight, 150.0, 12, 28, 0.90, 1.55);

            PC arenaSeatOne = findOrCreatePc(pcRepository, slotRepository, arena, 1, "PS5 + 55 inch 4K TV");
            PC arenaSeatTwo = findOrCreatePc(pcRepository, slotRepository, arena, 2, "PS5 Slim + Racing Setup");
            PC arenaSeatThree = findOrCreatePc(pcRepository, slotRepository, arena, 3, "PS5 Digital + FIFA Station");
            PC arenaSeatFour = findOrCreatePc(pcRepository, slotRepository, arena, 4, "PS5 + DualSense Edge Setup");
            PC midnightSeatOne = findOrCreatePc(pcRepository, slotRepository, midnight, 1, "PS4 Pro + FIFA Station");
            PC midnightSeatTwo = findOrCreatePc(pcRepository, slotRepository, midnight, 2, "PS5 + VR Corner");
            PC midnightSeatThree = findOrCreatePc(pcRepository, slotRepository, midnight, 3, "PS5 Slim + Story Mode Lounge");
            PC midnightSeatFour = findOrCreatePc(pcRepository, slotRepository, midnight, 4, "PS5 + Pro Controller Arena");

            seedPreferences(userGamePreferenceRepository, userOne, arena, List.of("EA Sports FC 25", "Tekken 8", "Spider-Man 2"));
            seedPreferences(userGamePreferenceRepository, userTwo, arena, List.of("EA Sports FC 25", "God of War Ragnarok", "Tekken 8"));
            seedPreferences(userGamePreferenceRepository, userOne, midnight, List.of("Gran Turismo 7", "Call of Duty"));
            seedPreferences(userGamePreferenceRepository, userTwo, midnight, List.of("Gran Turismo 7", "EA Sports FC 25", "Call of Duty"));
            seedPreferences(userGamePreferenceRepository, userThree, arena, List.of("EA Sports FC 25", "Call of Duty", "Tekken 8"));
            seedPreferences(userGamePreferenceRepository, userFour, midnight, List.of("Gran Turismo 7", "Spider-Man 2", "God of War Ragnarok"));
            seedPreferences(userGamePreferenceRepository, userFive, arena, List.of("Tekken 8", "Spider-Man 2", "EA Sports FC 25"));
            seedPreferences(userGamePreferenceRepository, userSix, arena, List.of("God of War Ragnarok", "EA Sports FC 25", "Call of Duty"));
            seedPreferences(userGamePreferenceRepository, userSeven, midnight, List.of("Tekken 8", "Gran Turismo 7", "Call of Duty"));
            seedPreferences(userGamePreferenceRepository, userEight, midnight, List.of("Spider-Man 2", "EA Sports FC 25", "Gran Turismo 7"));

            seedBookings(userBookingRepository, slotRepository, arenaSeatOne, userOne, LocalDate.now().minusDays(1), List.of(10, 12, 18));
            seedBookings(userBookingRepository, slotRepository, arenaSeatOne, userTwo, LocalDate.now(), List.of(14, 16));
            seedBookings(userBookingRepository, slotRepository, arenaSeatTwo, userOne, LocalDate.now().minusDays(2), List.of(11, 15));
            seedBookings(userBookingRepository, slotRepository, arenaSeatThree, userSix, LocalDate.now().minusDays(3), List.of(14, 19));
            seedBookings(userBookingRepository, slotRepository, arenaSeatFour, userSeven, LocalDate.now().minusDays(4), List.of(18, 20));
            seedBookings(userBookingRepository, slotRepository, midnightSeatOne, userTwo, LocalDate.now().minusDays(7), List.of(12, 17, 20));
            seedBookings(userBookingRepository, slotRepository, midnightSeatTwo, userOne, LocalDate.now().minusDays(14), List.of(13, 19, 21));
            seedBookings(userBookingRepository, slotRepository, midnightSeatThree, userEight, LocalDate.now().minusDays(5), List.of(15, 18));
            seedBookings(userBookingRepository, slotRepository, midnightSeatFour, userThree, LocalDate.now().minusDays(6), List.of(16, 22));

            seedOfflineBooking(offlineBookingRepository, arena, LocalDate.now(), 14, 3, "Walk-ins after school");
            seedOfflineBooking(offlineBookingRepository, arena, LocalDate.now().plusDays(1), 18, 2, "Tournament practice");
            seedOfflineBooking(offlineBookingRepository, midnight, LocalDate.now(), 20, 4, "Weekend crowd");

            for (int month = 1; month <= 6; month++) {
                PC targetPc = month % 2 == 0 ? arenaSeatOne : midnightSeatTwo;
                User targetUser = month % 2 == 0 ? userOne : userTwo;
                seedBookings(
                        userBookingRepository,
                        slotRepository,
                        targetPc,
                        targetUser,
                        LocalDate.of(LocalDate.now().getYear(), month, Math.min(10 + month, 28)),
                        List.of(12 + (month % 4), 17 + (month % 3))
                );
            }

            seedOptimizerScenarioData(
                    userBookingRepository,
                    offlineBookingRepository,
                    slotRepository,
                    arena,
                    List.of(arenaSeatOne, arenaSeatTwo, arenaSeatThree, arenaSeatFour),
                    List.of(userOne, userTwo, userThree, userFour, userFive, userSix, userSeven, userEight)
            );
            seedOptimizerScenarioData(
                    userBookingRepository,
                    offlineBookingRepository,
                    slotRepository,
                    midnight,
                    List.of(midnightSeatOne, midnightSeatTwo, midnightSeatThree, midnightSeatFour),
                    List.of(userOne, userTwo, userThree, userFour, userFive, userSix, userSeven, userEight)
            );
        };
    }

    private void seedGames(GameRepository gameRepository) {
        List<String> gameNames = List.of(
                "EA Sports FC 25",
                "Tekken 8",
                "Spider-Man 2",
                "God of War Ragnarok",
                "Gran Turismo 7",
                "Call of Duty"
        );

        for (String gameName : gameNames) {
            Game game = new Game();
            game.setName(gameName);
            gameRepository.save(game);
        }
    }

    private User findOrCreateUser(UserRepository userRepository, String name, String email, String phone) {
        Optional<User> existingUser = userRepository.findByEmail(email);
        if (existingUser.isPresent()) {
            return existingUser.get();
        }

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPhone(phone);
        user.setRoles("ROLE_USER");
        return userRepository.save(user);
    }

    private Cafe findOrCreateCafe(CafeRepository cafeRepository, CafeOwner owner, String name, String address, LocalTime openTime, LocalTime closeTime, Double hourlyRate, String imagePath) {
        List<Cafe> ownerCafes = cafeRepository.findAllByOwner(owner);
        for (Cafe existingCafe : ownerCafes) {
            if (name.equals(existingCafe.getName())) {
                return existingCafe;
            }
        }

        Cafe cafe = new Cafe();
        cafe.setOwner(owner);
        cafe.setName(name);
        cafe.setAddress(address);
        cafe.setOpenTime(openTime);
        cafe.setCloseTime(closeTime);
        cafe.setHourlyRate(hourlyRate);
        cafe.setCafeImage(imagePath);
        return cafeRepository.save(cafe);
    }

    private PC findOrCreatePc(PCRepository pcRepository, SlotRepository slotRepository, Cafe cafe, int seatNumber, String configuration) {
        List<PC> existingPcs = pcRepository.findByCafeId(cafe.getId());
        for (PC existingPc : existingPcs) {
            if (existingPc.getSeatNumber() == seatNumber) {
                if (slotRepository.findByPcId(existingPc.getId()).isEmpty()) {
                    seedSlots(slotRepository, cafe, existingPc);
                }
                return existingPc;
            }
        }

        PC pc = new PC();
        pc.setCafe(cafe);
        pc.setSeatNumber(seatNumber);
        pc.setConfiguration(configuration);
        pc.setAvailable("Available");
        pc = pcRepository.save(pc);

        seedSlots(slotRepository, cafe, pc);
        return pcRepository.findById(pc.getId()).orElse(pc);
    }

    private void seedSlots(SlotRepository slotRepository, Cafe cafe, PC pc) {
        LocalTime current = cafe.getOpenTime();
        while (current.isBefore(cafe.getCloseTime())) {
            Slot slot = new Slot();
            slot.setPc(pc);
            slot.setStartTime(current);
            slot.setEndTime(current.plusHours(1));
            slot.setBooked(false);
            slotRepository.save(slot);
            current = current.plusHours(1);
        }
    }

    private void seedPreferences(UserGamePreferenceRepository repository, User user, Cafe cafe, List<String> games) {
        List<UserGamePreference> existingPreferences = repository.findAll().stream()
                .filter(preference -> preference.getUser() != null && preference.getCafe() != null)
                .filter(preference -> preference.getUser().getId().equals(user.getId()) && preference.getCafe().getId().equals(cafe.getId()))
                .toList();

        List<String> existingNames = existingPreferences.stream()
                .map(UserGamePreference::getGameName)
                .toList();

        for (String gameName : games) {
            if (existingNames.contains(gameName)) {
                continue;
            }
            UserGamePreference preference = new UserGamePreference();
            preference.setUser(user);
            preference.setCafe(cafe);
            preference.setGameName(gameName);
            repository.save(preference);
        }
    }

    private void seedBookings(UserBookingRepository repository, SlotRepository slotRepository, PC pc, User user, LocalDate date, List<Integer> hours) {
        List<Slot> slotsForPc = slotRepository.findByPcId(pc.getId());
        for (Integer hour : hours) {
            Slot slot = slotsForPc.stream()
                    .filter(existingSlot -> existingSlot.getStartTime().getHour() == hour)
                    .findFirst()
                    .orElse(null);

            if (slot == null) {
                continue;
            }

            if (repository.existsBySlotIdAndBookingDateAndStatus(slot.getId(), date, UserBooking.BookingStatus.BOOKED)) {
                continue;
            }

            UserBooking booking = new UserBooking();
            booking.setPc(pc);
            booking.setSlot(slot);
            booking.setUser(user);
            booking.setBookingDate(date);
            booking.setStartTime(slot.getStartTime());
            booking.setEndTime(slot.getEndTime());
            booking.setStatus(UserBooking.BookingStatus.BOOKED);
            repository.save(booking);
        }
    }

    private void seedPricingRule(PricingRuleRepository repository,
                                 Cafe cafe,
                                 double basePrice,
                                 int lowThreshold,
                                 int highThreshold,
                                 double lowMultiplier,
                                 double highMultiplier) {
        if (repository.findByCafeId(cafe.getId()).isPresent()) {
            return;
        }

        PricingRule rule = new PricingRule();
        rule.setCafe(cafe);
        rule.setBasePrice(basePrice);
        rule.setLowDemandThreshold(lowThreshold);
        rule.setHighDemandThreshold(highThreshold);
        rule.setLowMultiplier(lowMultiplier);
        rule.setHighMultiplier(highMultiplier);
        repository.save(rule);
    }

    private void seedOfflineBooking(OfflineBookingRepository repository,
                                    Cafe cafe,
                                    LocalDate bookingDate,
                                    int timeSlot,
                                    int customerCount,
                                    String notes) {
        long existingCount = repository.sumCustomerCount(cafe.getId(), bookingDate, timeSlot);
        if (existingCount >= customerCount) {
            return;
        }

        OfflineBooking offlineBooking = new OfflineBooking();
        offlineBooking.setCafe(cafe);
        offlineBooking.setBookingDate(bookingDate);
        offlineBooking.setTimeSlot(timeSlot);
        offlineBooking.setCustomerCount((int) (customerCount - existingCount));
        offlineBooking.setNotes(notes);
        repository.save(offlineBooking);
    }

    private void seedOptimizerScenarioData(UserBookingRepository bookingRepository,
                                           OfflineBookingRepository offlineBookingRepository,
                                           SlotRepository slotRepository,
                                           Cafe cafe,
                                           List<PC> pcs,
                                           List<User> users) {
        for (int dayOffset = 0; dayOffset < 14; dayOffset++) {
            LocalDate date = LocalDate.now().plusDays(dayOffset);
            boolean weekend = date.getDayOfWeek().getValue() >= 5;

            seedDemandWindow(bookingRepository, offlineBookingRepository, slotRepository, cafe, pcs, users, date, 11, 0, 0 + (dayOffset % 2));
            seedDemandWindow(bookingRepository, offlineBookingRepository, slotRepository, cafe, pcs, users, date, 13, 1, 1);
            seedDemandWindow(bookingRepository, offlineBookingRepository, slotRepository, cafe, pcs, users, date, 14, 2, 2 + (dayOffset % 2));
            seedDemandWindow(bookingRepository, offlineBookingRepository, slotRepository, cafe, pcs, users, date, 16, 3, 2 + (dayOffset % 3));
            seedDemandWindow(
                    bookingRepository, offlineBookingRepository, slotRepository, cafe, pcs, users, date,
                    18,
                    4,
                    weekend ? 18 + (dayOffset % 5) : 24 + (dayOffset % 6)
            );
            seedDemandWindow(
                    bookingRepository, offlineBookingRepository, slotRepository, cafe, pcs, users, date,
                    20,
                    weekend ? 4 : 3,
                    weekend ? 14 + (dayOffset % 4) : 18 + (dayOffset % 4)
            );
            if (weekend) {
                seedDemandWindow(bookingRepository, offlineBookingRepository, slotRepository, cafe, pcs, users, date, 19, 4, 22 + (dayOffset % 5));
                seedDemandWindow(bookingRepository, offlineBookingRepository, slotRepository, cafe, pcs, users, date, 21, 4, 20 + (dayOffset % 4));
            } else {
                seedDemandWindow(bookingRepository, offlineBookingRepository, slotRepository, cafe, pcs, users, date, 17, 4, 20 + (dayOffset % 4));
                seedDemandWindow(bookingRepository, offlineBookingRepository, slotRepository, cafe, pcs, users, date, 19, 4, 26 + (dayOffset % 5));
            }
        }
    }

    private void seedDemandWindow(UserBookingRepository bookingRepository,
                                  OfflineBookingRepository offlineBookingRepository,
                                  SlotRepository slotRepository,
                                  Cafe cafe,
                                  List<PC> pcs,
                                  List<User> users,
                                  LocalDate date,
                                  int hour,
                                  int onlineCount,
                                  int offlineCount) {
        for (int i = 0; i < onlineCount && i < pcs.size() && i < users.size(); i++) {
            seedBookings(bookingRepository, slotRepository, pcs.get(i), users.get(i), date, List.of(hour));
        }

        if (offlineCount > 0) {
            seedOfflineBooking(
                    offlineBookingRepository,
                    cafe,
                    date,
                    hour,
                    offlineCount,
                    "Optimizer scenario seed for " + hour + ":00"
            );
        }
    }
}
