package com.Major.majorProject.service;

import com.Major.majorProject.dto.*;
import com.Major.majorProject.entity.*;
import com.Major.majorProject.repository.*;
import com.Major.majorProject.repository.ImageService;
import jakarta.transaction.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OwnerService {

    private final CafeOwnerRepository cafeOwnerRepository;
    private final CafeRepository cafeRepository;
    private final PCRepository pcRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserBookingRepository userBookingRepository;
    private final UserRepository userRepository;
    private final SlotRepository slotRepository;
    private final OfflineBookingRepository offlineBookingRepository;
    private final ImageService imageService;

    public OwnerService(CafeOwnerRepository cor, PasswordEncoder pe, CafeRepository cr, PCRepository pcr, UserBookingRepository ubr, UserRepository ur, SlotRepository slotRepository, OfflineBookingRepository offlineBookingRepository, ImageService is) {
        this.cafeOwnerRepository = cor;
        this.passwordEncoder = pe;
        this.cafeRepository = cr;
        this.pcRepository = pcr;
        this.userBookingRepository = ubr;
        this.userRepository = ur;
        this.slotRepository = slotRepository;
        this.offlineBookingRepository = offlineBookingRepository;
        this.imageService = is;
    }

    public void ownerRegistration(OwnerRegistrationDto ord) {
        CafeOwner owner = new CafeOwner();
        owner.setName(ord.getName());
        owner.setEmail(ord.getEmail());
        owner.setPhone(ord.getPhone());
        owner.setPassword(passwordEncoder.encode(ord.getPassword()));
        cafeOwnerRepository.save(owner);
    }

    private CafeOwner getCurrentOwner() {
        String ownerEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        return cafeOwnerRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new RuntimeException("Owner not found"));
    }

    private Cafe getOwnedCafe(long cafeId) {
        CafeOwner owner = getCurrentOwner();
        Cafe cafe = cafeRepository.findById(cafeId)
                .orElseThrow(() -> new RuntimeException("Cafe not found with ID: " + cafeId));

        if (cafe.getOwner() == null || !cafe.getOwner().getId().equals(owner.getId())) {
            throw new RuntimeException("You do not have access to this cafe.");
        }

        return cafe;
    }

    public void cafeAddition(CafeAdditionDto cad) {
        CafeOwner owner = getCurrentOwner();

        String filename = UUID.randomUUID().toString();
        String fileURL = imageService.uploadImage(cad.getCafeImageFile(), filename);
        Cafe cafe = new Cafe();
        cafe.setName(cad.getName());
        cafe.setAddress(cad.getAddress());
        cafe.setOpenTime(cad.getOpenTime());
        cafe.setCloseTime(cad.getCloseTime());
        cafe.setHourlyRate(cad.getHourlyRate());
        cafe.setCafeImage(fileURL);
        cafe.setAmenities(parseAmenities(cad.getAmenitiesInput()));
        cafe.setOwner(owner);
        cafeRepository.save(cafe);
    }

    public CafeAdditionDto getCafeForEdit(long cafeId) {
        Cafe cafe = getOwnedCafe(cafeId);
        CafeAdditionDto dto = new CafeAdditionDto();
        dto.setId(cafe.getId());
        dto.setName(cafe.getName());
        dto.setAddress(cafe.getAddress());
        dto.setOpenTime(cafe.getOpenTime());
        dto.setCloseTime(cafe.getCloseTime());
        dto.setHourlyRate(cafe.getHourlyRate());
        dto.setCafeImage(cafe.getCafeImage());
        dto.setAmenities(cafe.getAmenities());
        dto.setAmenitiesInput(String.join(", ", cafe.getAmenities()));
        return dto;
    }

    @Transactional
    public void updateCafe(long cafeId, CafeAdditionDto dto) {
        Cafe cafe = getOwnedCafe(cafeId);
        cafe.setName(dto.getName());
        cafe.setAddress(dto.getAddress());
        cafe.setOpenTime(dto.getOpenTime());
        cafe.setCloseTime(dto.getCloseTime());
        cafe.setHourlyRate(dto.getHourlyRate());
        cafe.setAmenities(parseAmenities(dto.getAmenitiesInput()));

        if (dto.getCafeImageFile() != null && !dto.getCafeImageFile().isEmpty()) {
            String fileURL = imageService.uploadImage(dto.getCafeImageFile(), UUID.randomUUID().toString());
            if (fileURL != null && !fileURL.isBlank()) {
                cafe.setCafeImage(fileURL);
            }
        }

        cafeRepository.save(cafe);
    }

    @Transactional
    public void deleteCafe(long cafeId) {
        Cafe cafe = getOwnedCafe(cafeId);
        if (userBookingRepository.existsByPcCafeId(cafeId)) {
            throw new RuntimeException("Cannot delete a cafe that has booking history.");
        }
        cafeRepository.delete(cafe);
    }

    public CafeOwner findByEmail(String email) {
        return cafeOwnerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Owner not found with email: " + email));
    }

    public List<CafeAdditionDto> getAllCafeOfOwner() {
        CafeOwner owner = getCurrentOwner();

        if (owner.getCafes() == null) {
            return Collections.emptyList();
        }

        return owner.getCafes().stream().map(cafe -> {
            CafeAdditionDto dto = new CafeAdditionDto();
            dto.setId(cafe.getId());
            dto.setName(cafe.getName());
            dto.setAddress(cafe.getAddress());
            dto.setOpenTime(cafe.getOpenTime());
            dto.setCloseTime(cafe.getCloseTime());
            dto.setHourlyRate(cafe.getHourlyRate());
            dto.setCafeImage(cafe.getCafeImage());
            dto.setAmenities(cafe.getAmenities());
            // Calculate available PCs
            long availableCount = cafe.getPcs().stream()
                    .map(pc -> getAccuratePcAvailability(pc.getId()))
                    .filter(status -> status.equals("Available"))
                    .count();
            dto.setAvailablePcs((int) availableCount);
            return dto;
        }).collect(Collectors.toList());
    }

    public List<PCDto> getAllPcOfCafe(long cafeId) {
        List<PC> pcs = pcRepository.findByCafeId(cafeId);
        return pcs.stream().map(this::mapPcToPcDto).collect(Collectors.toList());
    }

    public List<CafeAdditionDto> getAllCafes() {
        return cafeRepository.findAll().stream().map(cafe -> {
            CafeAdditionDto dto = new CafeAdditionDto();
            dto.setId(cafe.getId());
            dto.setName(cafe.getName());
            dto.setAddress(cafe.getAddress());
            dto.setOpenTime(cafe.getOpenTime());
            dto.setCloseTime(cafe.getCloseTime());
            dto.setHourlyRate(cafe.getHourlyRate());
            long availableCount = cafe.getPcs().stream()
                    .map(pc -> getAccuratePcAvailability(pc.getId()))
                    .filter(status -> status.equals("Available"))
                    .count();
            dto.setAvailablePcs((int) availableCount);
            dto.setCafeImage(cafe.getCafeImage());
            dto.setAmenities(cafe.getAmenities());
            return dto;
        }).collect(Collectors.toList());
    }

    public CafeAdditionDto getCafeDtoById(long cafeId) {
        Cafe cafe = cafeRepository.findById(cafeId)
                .orElseThrow(() -> new RuntimeException("Cafe not found with ID: " + cafeId));
        CafeAdditionDto dto = new CafeAdditionDto();
        dto.setId(cafe.getId());
        dto.setName(cafe.getName());
        dto.setAddress(cafe.getAddress());
        dto.setOpenTime(cafe.getOpenTime());
        dto.setCloseTime(cafe.getCloseTime());
        dto.setHourlyRate(cafe.getHourlyRate());
        dto.setCafeImage(cafe.getCafeImage());
        dto.setAmenities(cafe.getAmenities());
        return dto;
    }

    public String getPcAvailability(Long pcId) {
        PC pc = pcRepository.findById(pcId).orElseThrow(() -> new RuntimeException("PC not found"));
        Cafe cafe = pc.getCafe();
        List<UserBooking> bookings = userBookingRepository.findByPcIdAndBookingDate(pcId, LocalDate.now());

        int totalSlots = cafe.getCloseTime().getHour() - cafe.getOpenTime().getHour();
        if (bookings.isEmpty()) {
            return "Available";
        } else if (bookings.size() >= totalSlots) {
            return "Full";
        } else {
            return "Busy";
        }
    }

    public String getAccuratePcAvailability(Long pcId) {
        // 1. Get all slots for this PC from the database
        List<Slot> allSlotsForPc = slotRepository.findByPcId(pcId);
        if (allSlotsForPc.isEmpty()) {
            return "Unavailable"; // Or "Full" if no slots are configured
        }

        // 2. Filter to find only the slots that are still in the future today
        List<Slot> futureSlots = allSlotsForPc.stream()
                .filter(slot -> !slot.getEndTime().isBefore(LocalTime.now()))
                .toList();

        if (futureSlots.isEmpty()) {
            return "Full"; // All slots for today are in the past
        }

        // 3. Find all active bookings (BOOKED or non-expired PENDING) for these future slots
        List<Long> futureSlotIds = futureSlots.stream().map(Slot::getId).toList();
        
        List<UserBooking> activeBookings = userBookingRepository.findActiveBookingsForSlots(
            futureSlotIds,
            UserBooking.BookingStatus.BOOKED,
            UserBooking.BookingStatus.PENDING,
            LocalDateTime.now()
        );

        // 4. Compare the counts to determine the final status
        if (activeBookings.isEmpty()) {
            return "Available"; // No active bookings for any future slots
        } else if (activeBookings.size() >= futureSlots.size()) {
            return "Full"; // All available future slots are booked or pending
        } else {
            return "Busy"; // Some slots are booked/pending, but others are still free
        }
    }

    public List<LocalTime> getAvailableSlotsForPC(long pcId) {
        PC pc = pcRepository.findById(pcId).orElseThrow(() -> new RuntimeException("PC not found with id: " + pcId));
        Cafe cafe = pc.getCafe();
        LocalTime openTime = cafe.getOpenTime();
        LocalTime closeTime = cafe.getCloseTime();

        List<UserBooking> todaysBookings = userBookingRepository.findByPcIdAndBookingDate(pcId, LocalDate.now());
        List<LocalTime> bookedStartTimes = todaysBookings.stream().map(UserBooking::getStartTime).toList();

        List<LocalTime> availableSlots = new ArrayList<>();
        LocalTime potentialStartTime = openTime;
        LocalTime now = LocalTime.now();

        while (potentialStartTime.isBefore(closeTime)) {
            LocalTime potentialEndTime = potentialStartTime.plusHours(1);

            if (!bookedStartTimes.contains(potentialStartTime) &&
                    !potentialStartTime.isBefore(now) &&
                    !potentialEndTime.isAfter(closeTime)) {
                availableSlots.add(potentialStartTime);
            }

            potentialStartTime = potentialStartTime.plusHours(1);
        }
        return availableSlots;
    }


    public void bookSlot(long pcId, LocalTime startTime) {
        PC pc = pcRepository.findById(pcId)
                .orElseThrow(() -> new RuntimeException("PC not found for booking."));

        UserBooking booking = new UserBooking();
        booking.setPc(pc);
        //booking.setUser(user);
        booking.setBookingDate(LocalDate.now());
        booking.setStartTime(startTime);
        booking.setEndTime(startTime.plusHours(1));

        userBookingRepository.save(booking);
    }

    public PCDto findPCById(long pcId) {
        PC pc = pcRepository.findById(pcId)
                .orElseThrow(() -> new RuntimeException("PC not found with ID: " + pcId));
        return mapPcToPcDto(pc);
    }

    // private PCDto mapPcToPcDto(PC pc) {
    //     PCDto pcDto = new PCDto();
    //     pcDto.setId(pc.getId());
    //     pcDto.setSeatNumber(pc.getSeatNumber());
    //     pcDto.setConfiguration(pc.getConfiguration());
    //     pcDto.setAvailable(getPcAvailability(pc.getId()));
    //     pcDto.setCafeId(pc.getCafe().getId()); // Ensure you're setting the cafeId here
    //     pcDto.setCafeName(pc.getCafe().getName());
    //     return pcDto;
    // }

    private PCDto mapPcToPcDto(PC pc) {
    PCDto pcDto = new PCDto();
    pcDto.setId(pc.getId());
    pcDto.setSeatNumber(pc.getSeatNumber());
    pcDto.setConfiguration(pc.getConfiguration());
    pcDto.setAvailable(getAccuratePcAvailability(pc.getId())); // <-- CHANGED to call the NEW method
    pcDto.setCafeId(pc.getCafe().getId());
    pcDto.setCafeName(pc.getCafe().getName());
    return pcDto;
}

    public List<SlotDetails> getAllSlotsOfPc(long pcId) {
        PC pc = pcRepository.findById(pcId).orElseThrow(() -> new RuntimeException("PC not found"));
        Cafe cafe = pc.getCafe();
        LocalTime openTime = cafe.getOpenTime();
        LocalTime closeTime = cafe.getCloseTime();

        List<UserBooking> bookings = userBookingRepository.findByPcIdAndBookingDate(pcId, LocalDate.now());

        List<SlotDetails> slots = new ArrayList<>();
        LocalTime currentTime = openTime;

        while (currentTime.isBefore(closeTime)) {
            SlotDetails slot = new SlotDetails();
            slot.setStartTime(currentTime);
            LocalTime endTime = currentTime.plusHours(1);
            slot.setEndTime(endTime);
            slot.setStatus("open");
            slot.setCafeId(cafe.getId());

            for (UserBooking booking : bookings) {
                if (currentTime.isBefore(booking.getEndTime()) && endTime.isAfter(booking.getStartTime())) {
                    slot.setStatus("closed");
                    break;
                }
            }
            slots.add(slot);
            currentTime = endTime;
        }
        return slots;
    }

    public void addPC(long cafeId, PCDto pcdto) {
        Cafe cafe = cafeRepository.findById(cafeId)
                .orElseThrow(() -> new RuntimeException("Cafe not found"));
        PC pc = new PC();
        pc.setSeatNumber(pcdto.getSeatNumber());
        pc.setConfiguration(pcdto.getConfiguration());
        pc.setAvailable("Available");
        pc.setCafe(cafe);
        PC savedPC = pcRepository.save(pc);
        generateSlotsForPC(savedPC, cafe.getOpenTime(), cafe.getCloseTime());
    }

    private void generateSlotsForPC(PC pc, LocalTime openTime, LocalTime closeTime) {
        LocalTime currentTime = openTime;
        while (currentTime.isBefore(closeTime)) {
            Slot slot = new Slot();
            slot.setPc(pc);
            slot.setStartTime(currentTime);
            slot.setEndTime(currentTime.plusHours(1));
            slot.setBooked(false);
            slotRepository.save(slot);
            currentTime = currentTime.plusHours(1);
        }
    }

    public PC getPCById(long pcId) {
        return pcRepository.findById(pcId).orElse(null);
    }

    public List<SlotDetails> getSlotsForPC(long pcId, LocalDate selectedDate) {
        List<Slot> savedSlots = slotRepository.findByPcId(pcId);

        if (savedSlots.isEmpty()) {
            return Collections.emptyList();
        }

        PC pc = getPCById(pcId);
        long cafeId = (pc != null && pc.getCafe() != null) ? pc.getCafe().getId() : 0;

        return savedSlots.stream()
                .map(slot -> {
                    SlotDetails details = new SlotDetails();
                    details.setId(slot.getId());
                    details.setStartTime(slot.getStartTime());
                    details.setEndTime(slot.getEndTime());
                    details.setCafeId(cafeId);

                    boolean isBookedForSelectedDate = userBookingRepository.existsBySlotIdAndBookingDateAndStatus(
                            slot.getId(), selectedDate, UserBooking.BookingStatus.BOOKED);
                    if (isBookedForSelectedDate) {
                        details.setStatus("booked");
                        userBookingRepository.findBySlotIdAndBookingDateAndStatusInOrderByIdDesc(
                                        slot.getId(),
                                        selectedDate,
                                        List.of(UserBooking.BookingStatus.BOOKED, UserBooking.BookingStatus.NO_SHOW))
                                .stream()
                                .findFirst()
                                .ifPresent(booking -> details.setBookingId(booking.getId()));
                    } else if (selectedDate.equals(LocalDate.now()) && slot.getEndTime().isBefore(LocalTime.now())) {
                        details.setStatus("past");
                    } else {
                        details.setStatus("open");
                    }
                    return details;
                })
                .sorted(Comparator.comparing(SlotDetails::getStartTime))
                .collect(Collectors.toList());
    }

    public BookingReceiptDto getBookingReceiptForOwner(Long bookingId) {
        UserBooking booking = userBookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        getOwnedCafe(booking.getPc().getCafe().getId());

        BookingReceiptDto dto = new BookingReceiptDto();
        dto.setBookingId(booking.getId());
        dto.setCafeName(booking.getPc().getCafe().getName());
        dto.setSeatNumber(booking.getPc().getSeatNumber());
        dto.setBookingDate(booking.getBookingDate());
        dto.setStartTime(booking.getStartTime());
        dto.setEndTime(booking.getEndTime());
        dto.setStatus(booking.getStatus().name());
        if (booking.getUser() != null) {
            dto.setCustomerName(booking.getUser().getName());
            dto.setCustomerEmail(booking.getUser().getEmail());
            dto.setCustomerPhone(booking.getUser().getPhone());
        }
        return dto;
    }

    @Transactional
    public void addSlots(SlotDto slotDto) {
        PC pc = pcRepository.findById(slotDto.getPcId())
                .orElseThrow(() -> new RuntimeException("PC not found with id: " + slotDto.getPcId()));

        if (pc.getSlots() == null) {
            pc.setSlots(new ArrayList<>());
        }

        for (LocalTime startTime : slotDto.getStartTime()) {
            Slot slot = new Slot();
            slot.setPc(pc); // Set the PC on the slot
            slot.setStartTime(startTime);
            slot.setEndTime(startTime.plusHours(1)); // Assuming 1-hour slots
            slot.setBooked(false);

            pc.getSlots().add(slot); // Add the new slot to the PC's list

            slotRepository.save(slot);
        }
    }

    @Transactional
    public void updateSlot(Long slotId, LocalTime startTime) {
        Slot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new RuntimeException("PC not found with id: " + slotId));

        if (userBookingRepository.existsBySlotIdAndStatusIn(
                slotId,
                List.of(UserBooking.BookingStatus.BOOKED, UserBooking.BookingStatus.PENDING)
        )) {
            throw new RuntimeException("Cannot edit a slot that has an active booking. Mark it as no-show first if the user did not arrive.");
        }

        slot.setStartTime(startTime);
        slot.setEndTime(startTime.plusHours(1));
        slotRepository.save(slot);
    }

    @Transactional
    public void deleteSlot(Long slotId) {
        if (userBookingRepository.existsBySlotIdAndStatusIn(
                slotId,
                List.of(UserBooking.BookingStatus.BOOKED, UserBooking.BookingStatus.PENDING)
        )) {
            throw new RuntimeException("Cannot delete a slot that has an active booking. Mark it as no-show first if the user did not arrive.");
        }

        slotRepository.deleteById(slotId);
    }

    @Transactional
    public void markNoShowAndAddWalkIn(Long slotId, LocalDate bookingDate) {
        Slot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new RuntimeException("Slot not found with id: " + slotId));

        LocalDate selectedDate = bookingDate != null ? bookingDate : LocalDate.now();
        UserBooking booking = userBookingRepository.findBySlotIdAndBookingDateAndStatus(
                        slotId, selectedDate, UserBooking.BookingStatus.BOOKED)
                .orElseThrow(() -> new RuntimeException("No confirmed booking found for this slot on the selected date."));

        booking.setStatus(UserBooking.BookingStatus.NO_SHOW);
        userBookingRepository.save(booking);

        OfflineBooking walkIn = new OfflineBooking();
        walkIn.setCafe(slot.getPc().getCafe());
        walkIn.setBookingDate(selectedDate);
        walkIn.setTimeSlot(slot.getStartTime().getHour());
        walkIn.setCustomerCount(1);
        walkIn.setNotes("Walk-in replacement after no-show for PC " + slot.getPc().getSeatNumber());
        offlineBookingRepository.save(walkIn);
    }

    private List<String> parseAmenities(String amenitiesInput) {
        if (amenitiesInput == null || amenitiesInput.isBlank()) {
            return new ArrayList<>();
        }

        return Arrays.stream(amenitiesInput.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .distinct()
                .collect(Collectors.toCollection(ArrayList::new));
    }

}
