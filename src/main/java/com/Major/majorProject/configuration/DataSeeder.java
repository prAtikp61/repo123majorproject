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
import java.util.Arrays;
import java.util.List;

@Configuration
public class DataSeeder {

    private static final List<String> IMAGE_POOL = List.of(
            "/images/cafe1.jpeg",
            "/images/cafe2.jpg",
            "/images/cafe3.jpg"
    );

    private static final List<String> GAME_POOL = List.of(
            "EA Sports FC 25",
            "Tekken 8",
            "Spider-Man 2",
            "God of War Ragnarok",
            "Gran Turismo 7",
            "Call of Duty",
            "Mortal Kombat 1",
            "WWE 2K24"
    );

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
            seedGames(gameRepository);

            List<CafeOwner> owners = seedOwners(cafeOwnerRepository, passwordEncoder);
            List<User> users = seedUsers(userRepository);
            List<Cafe> cafes = seedCafes(cafeRepository, owners);

            for (int index = 0; index < cafes.size(); index++) {
                Cafe cafe = cafes.get(index);
                seedPricingRule(pricingRuleRepository, cafe, index);
                List<PC> pcs = seedPcs(pcRepository, slotRepository, cafe, index);
                seedPreferences(userGamePreferenceRepository, users, cafe, index);
                seedBookings(userBookingRepository, offlineBookingRepository, slotRepository, cafe, pcs, users, index);
            }
        };
    }

    private void seedGames(GameRepository gameRepository) {
        for (String gameName : GAME_POOL) {
            boolean exists = gameRepository.findAll().stream().anyMatch(game -> gameName.equals(game.getName()));
            if (!exists) {
                Game game = new Game();
                game.setName(gameName);
                gameRepository.save(game);
            }
        }
    }

    private List<CafeOwner> seedOwners(CafeOwnerRepository repository, PasswordEncoder passwordEncoder) {
        List<CafeOwner> owners = new ArrayList<>();

        for (int index = 1; index <= 18; index++) {
            String padded = String.format("%02d", index);
            String email = "admin" + padded + "@pslounge.demo";
            String phone = "900000" + String.format("%04d", index);

            CafeOwner owner = repository.findByEmail(email).orElseGet(() -> {
                CafeOwner newOwner = new CafeOwner();
                newOwner.setName("Demo Admin " + padded);
                newOwner.setEmail(email);
                newOwner.setPhone(phone);
                newOwner.setPassword(passwordEncoder.encode("Admin@123"));
                newOwner.setStripeAccountId("demo_stripe_" + padded);
                newOwner.setRazorpayAccountId("demo_razorpay_" + padded);
                return repository.save(newOwner);
            });

            owners.add(owner);
        }

        return owners;
    }

    private List<User> seedUsers(UserRepository repository) {
        List<String[]> users = List.of(
                new String[]{"Aarav Patil", "aarav@example.com", "9999990001"},
                new String[]{"Riya Shah", "riya@example.com", "9999990002"},
                new String[]{"Kabir Mehta", "kabir@example.com", "9999990003"},
                new String[]{"Sneha Kulkarni", "sneha@example.com", "9999990004"},
                new String[]{"Yash Deshmukh", "yash@example.com", "9999990005"},
                new String[]{"Isha Verma", "isha@example.com", "9999990006"},
                new String[]{"Manav Joshi", "manav@example.com", "9999990007"},
                new String[]{"Pooja Nair", "pooja@example.com", "9999990008"},
                new String[]{"Arjun Rao", "arjun@example.com", "9999990009"},
                new String[]{"Neha Kapoor", "neha@example.com", "9999990010"},
                new String[]{"Dev Malhotra", "dev@example.com", "9999990011"},
                new String[]{"Siya Trivedi", "siya@example.com", "9999990012"},
                new String[]{"Omkar Jadhav", "omkar@example.com", "9999990013"},
                new String[]{"Tanya Arora", "tanya@example.com", "9999990014"},
                new String[]{"Harsh Soni", "harsh@example.com", "9999990015"},
                new String[]{"Zoya Khan", "zoya@example.com", "9999990016"},
                new String[]{"Rohan Iyer", "rohan@example.com", "9999990017"},
                new String[]{"Mira Patel", "mira@example.com", "9999990018"},
                new String[]{"Nikhil Sen", "nikhil@example.com", "9999990019"},
                new String[]{"Aditi Bose", "aditi@example.com", "9999990020"}
        );

        List<User> result = new ArrayList<>();
        for (String[] entry : users) {
            User user = repository.findByEmail(entry[1]).orElseGet(() -> {
                User newUser = new User();
                newUser.setName(entry[0]);
                newUser.setEmail(entry[1]);
                newUser.setPhone(entry[2]);
                newUser.setRoles("ROLE_USER");
                return repository.save(newUser);
            });
            result.add(user);
        }
        return result;
    }

    private List<Cafe> seedCafes(CafeRepository repository, List<CafeOwner> owners) {
        List<String[]> cafeSpecs = List.of(
                new String[]{"Arena PS Lounge", "Andheri West, Mumbai", "10:00", "22:00", "120"},
                new String[]{"Midnight Console Hub", "Bandra West, Mumbai", "11:00", "23:00", "150"},
                new String[]{"Elite Gamer Spot", "Lower Parel, Mumbai", "10:00", "23:00", "140"},
                new String[]{"Respawn Republic", "Powai, Mumbai", "09:00", "22:00", "110"},
                new String[]{"Victory Vault", "Ghatkopar East, Mumbai", "10:00", "22:00", "125"},
                new String[]{"Trigger Zone", "Chembur, Mumbai", "11:00", "23:00", "135"},
                new String[]{"Joystick Junction", "Dadar West, Mumbai", "10:00", "21:00", "100"},
                new String[]{"Pixel Playhouse", "Vashi, Navi Mumbai", "09:00", "22:00", "115"},
                new String[]{"PowerUp Lounge", "Nerul, Navi Mumbai", "10:00", "22:00", "118"},
                new String[]{"Checkpoint Cafe", "Seawoods, Navi Mumbai", "10:00", "23:00", "145"},
                new String[]{"Lag Free Arena", "Kharghar, Navi Mumbai", "09:00", "23:00", "155"},
                new String[]{"Combo Break Hub", "Belapur, Navi Mumbai", "10:00", "22:00", "130"},
                new String[]{"Next Gen Den", "Airoli, Navi Mumbai", "10:00", "23:00", "150"},
                new String[]{"Respawn District", "Ghansoli, Navi Mumbai", "11:00", "23:00", "142"},
                new String[]{"XP Lounge", "Thane West, Thane", "10:00", "22:00", "122"},
                new String[]{"Boss Fight Base", "Naupada, Thane", "10:00", "21:00", "108"},
                new String[]{"Console Collective", "Majiwada, Thane", "09:00", "22:00", "112"},
                new String[]{"PlayGrid Studio", "Kasarvadavali, Thane", "10:00", "22:00", "126"},
                new String[]{"Meta Room", "Wagle Estate, Thane", "10:00", "23:00", "148"},
                new String[]{"Power Circle Cafe", "Mira Road East, Mumbai", "10:00", "22:00", "132"},
                new String[]{"Champion's Corner", "Borivali West, Mumbai", "09:00", "23:00", "138"}
        );

        List<Cafe> cafes = new ArrayList<>();
        for (int index = 0; index < cafeSpecs.size(); index++) {
            String[] spec = cafeSpecs.get(index);
            CafeOwner owner = owners.get(index % owners.size());
            String imagePath = IMAGE_POOL.get(index % IMAGE_POOL.size());
            Cafe cafe = repository.findAllByOwner(owner).stream()
                    .filter(existing -> spec[0].equals(existing.getName()))
                    .findFirst()
                    .map(existing -> {
                        existing.setAddress(spec[1]);
                        existing.setOpenTime(LocalTime.parse(spec[2]));
                        existing.setCloseTime(LocalTime.parse(spec[3]));
                        existing.setHourlyRate(Double.parseDouble(spec[4]));
                        existing.setCafeImage(imagePath);
                        existing.setAmenities(List.of("4K Screens", "Air Conditioning", "Snacks", "Racing Setup"));
                        return repository.save(existing);
                    })
                    .orElseGet(() -> {
                        Cafe newCafe = new Cafe();
                        newCafe.setOwner(owner);
                        newCafe.setName(spec[0]);
                        newCafe.setAddress(spec[1]);
                        newCafe.setOpenTime(LocalTime.parse(spec[2]));
                        newCafe.setCloseTime(LocalTime.parse(spec[3]));
                        newCafe.setHourlyRate(Double.parseDouble(spec[4]));
                        newCafe.setCafeImage(imagePath);
                        newCafe.setAmenities(List.of("4K Screens", "Air Conditioning", "Snacks", "Racing Setup"));
                        return repository.save(newCafe);
                    });
            cafes.add(cafe);
        }
        return cafes;
    }

    private void seedPricingRule(PricingRuleRepository repository, Cafe cafe, int index) {
        repository.findByCafeId(cafe.getId()).orElseGet(() -> {
            PricingRule rule = new PricingRule();
            rule.setCafe(cafe);
            rule.setBasePrice(cafe.getHourlyRate());
            rule.setLowDemandThreshold(8 + (index % 4));
            rule.setHighDemandThreshold(18 + (index % 6));
            rule.setLowMultiplier(0.80 + ((index % 3) * 0.05));
            rule.setHighMultiplier(1.35 + ((index % 4) * 0.08));
            return repository.save(rule);
        });
    }

    private List<PC> seedPcs(PCRepository pcRepository, SlotRepository slotRepository, Cafe cafe, int cafeIndex) {
        List<PC> existingPcs = pcRepository.findByCafeId(cafe.getId());
        if (!existingPcs.isEmpty()) {
            existingPcs.forEach(pc -> ensureSlots(slotRepository, cafe, pc));
            return existingPcs;
        }

        List<String> configs = Arrays.asList(
                "PS5 + 55 inch 4K TV",
                "PS5 Slim + Racing Setup",
                "PS5 Digital + FIFA Station",
                "PS5 + DualSense Edge Setup"
        );

        List<PC> pcs = new ArrayList<>();
        for (int seat = 1; seat <= 4; seat++) {
            PC pc = new PC();
            pc.setCafe(cafe);
            pc.setSeatNumber(seat);
            pc.setConfiguration(configs.get((cafeIndex + seat - 1) % configs.size()));
            pc.setAvailable("Available");
            pc = pcRepository.save(pc);
            ensureSlots(slotRepository, cafe, pc);
            pcs.add(pc);
        }
        return pcs;
    }

    private void ensureSlots(SlotRepository slotRepository, Cafe cafe, PC pc) {
        if (!slotRepository.findByPcId(pc.getId()).isEmpty()) {
            return;
        }

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

    private void seedPreferences(UserGamePreferenceRepository repository, List<User> users, Cafe cafe, int cafeIndex) {
        for (int i = 0; i < Math.min(8, users.size()); i++) {
            User user = users.get((cafeIndex + i) % users.size());
            List<String> picks = List.of(
                    GAME_POOL.get((cafeIndex + i) % GAME_POOL.size()),
                    GAME_POOL.get((cafeIndex + i + 1) % GAME_POOL.size()),
                    GAME_POOL.get((cafeIndex + i + 2) % GAME_POOL.size())
            );

            List<String> existingNames = repository.findAll().stream()
                    .filter(preference -> preference.getUser() != null && preference.getCafe() != null)
                    .filter(preference -> preference.getUser().getId().equals(user.getId()) && preference.getCafe().getId().equals(cafe.getId()))
                    .map(UserGamePreference::getGameName)
                    .toList();

            for (String gameName : picks) {
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
    }

    private void seedBookings(UserBookingRepository bookingRepository,
                              OfflineBookingRepository offlineBookingRepository,
                              SlotRepository slotRepository,
                              Cafe cafe,
                              List<PC> pcs,
                              List<User> users,
                              int cafeIndex) {
        List<Integer> activeHours = List.of(11, 13, 15, 17, 18, 19, 20, 21);

        for (int dayOffset = -10; dayOffset <= 12; dayOffset++) {
            LocalDate date = LocalDate.now().plusDays(dayOffset);
            boolean weekend = date.getDayOfWeek().getValue() >= 6;

            for (int hour : activeHours) {
                int onlineCount = getOnlineDemandPattern(hour, weekend, cafeIndex, dayOffset);
                int offlineCount = getOfflineDemandPattern(hour, weekend, cafeIndex, dayOffset);

                for (int i = 0; i < Math.min(onlineCount, pcs.size()); i++) {
                    PC pc = pcs.get(i);
                    User user = users.get((cafeIndex + dayOffset + i + users.size()) % users.size());
                    seedBooking(bookingRepository, slotRepository, pc, user, date, hour);
                }

                if (offlineCount > 0) {
                    seedOfflineBooking(offlineBookingRepository, cafe, date, hour, offlineCount, "Showcase walk-in demand at " + hour + ":00");
                }
            }
        }
    }

    private int getOnlineDemandPattern(int hour, boolean weekend, int cafeIndex, int dayOffset) {
        int normalizedOffset = Math.abs(dayOffset) % 3;
        if (hour >= 19) {
            return weekend ? 4 : 3 + ((cafeIndex + normalizedOffset) % 2);
        }
        if (hour >= 17) {
            return weekend ? 3 : 2 + ((cafeIndex + normalizedOffset) % 2);
        }
        if (hour >= 13) {
            return 2 + ((cafeIndex + hour + normalizedOffset) % 2);
        }
        return 1 + ((cafeIndex + normalizedOffset) % 2);
    }

    private int getOfflineDemandPattern(int hour, boolean weekend, int cafeIndex, int dayOffset) {
        int normalizedOffset = Math.abs(dayOffset) % 4;
        if (hour >= 20) {
            return weekend ? 12 + ((cafeIndex + normalizedOffset) % 5) : 7 + ((cafeIndex + normalizedOffset) % 4);
        }
        if (hour >= 18) {
            return weekend ? 10 + ((cafeIndex + normalizedOffset) % 4) : 6 + ((cafeIndex + normalizedOffset) % 3);
        }
        if (hour >= 15) {
            return 3 + ((cafeIndex + normalizedOffset) % 3);
        }
        return 1 + ((cafeIndex + normalizedOffset) % 2);
    }

    private void seedBooking(UserBookingRepository repository,
                             SlotRepository slotRepository,
                             PC pc,
                             User user,
                             LocalDate date,
                             int hour) {
        Slot slot = slotRepository.findByPcId(pc.getId()).stream()
                .filter(existingSlot -> existingSlot.getStartTime().getHour() == hour)
                .findFirst()
                .orElse(null);

        if (slot == null) {
            return;
        }

        if (repository.existsBySlotIdAndBookingDateAndStatus(slot.getId(), date, UserBooking.BookingStatus.BOOKED)) {
            return;
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
}
