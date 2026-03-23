package com.Major.majorProject.controller;

import com.Major.majorProject.dto.CafeAdditionDto;
import com.Major.majorProject.dto.OfflineBookingDto;
import com.Major.majorProject.dto.PCDto;
import com.Major.majorProject.dto.PricingRuleDto;
import com.Major.majorProject.dto.SlotDetails;
import com.Major.majorProject.dto.SlotDto;
import com.Major.majorProject.entity.OfflineBooking;
import com.Major.majorProject.entity.PC;
import com.Major.majorProject.service.OfflineBookingService;
import com.Major.majorProject.service.OwnerService;
import com.Major.majorProject.service.PricingRuleService;
import org.springframework.stereotype.Controller; // Changed from RestController
import org.springframework.ui.Model; // Added Model
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Controller // Changed from RestController
@RequestMapping("/owner")
public class OwnerController {

    private final OwnerService ownerService;
    private final PricingRuleService pricingRuleService;
    private final OfflineBookingService offlineBookingService;

    public OwnerController(OwnerService os,
                           PricingRuleService pricingRuleService,
                           OfflineBookingService offlineBookingService){
        this.ownerService = os;
        this.pricingRuleService = pricingRuleService;
        this.offlineBookingService = offlineBookingService;
    }

    @GetMapping("/")
    public String ownerDashboard(Model model){
        List<CafeAdditionDto> cafes = ownerService.getAllCafeOfOwner();
        model.addAttribute("cafes", cafes);
        return "owner/ownerDashboard";
    }

    @GetMapping("/addCafe")
    public String showAddCafeForm(Model model){
        model.addAttribute("cafeAdditionDto", new CafeAdditionDto());
        return "owner/addCafe";
    }

    @PostMapping("/addCafe")
    public String cafeAddition(@ModelAttribute("cafeAdditionDto") CafeAdditionDto cad ){
        ownerService.cafeAddition(cad);
        return "redirect:/owner/";
    }

    @GetMapping("/editCafe/{cafeId}")
    public String showEditCafeForm(@PathVariable("cafeId") long cafeId, Model model) {
        model.addAttribute("cafeAdditionDto", ownerService.getCafeForEdit(cafeId));
        model.addAttribute("cafeId", cafeId);
        return "owner/editCafe";
    }

    @PostMapping("/editCafe/{cafeId}")
    public String updateCafe(@PathVariable("cafeId") long cafeId,
                             @ModelAttribute("cafeAdditionDto") CafeAdditionDto cad) {
        ownerService.updateCafe(cafeId, cad);
        return "redirect:/owner/cafes";
    }

    @PostMapping("/deleteCafe/{cafeId}")
    public String deleteCafe(@PathVariable("cafeId") long cafeId) {
        ownerService.deleteCafe(cafeId);
        return "redirect:/owner/cafes";
    }

    @GetMapping("/cafes")
    public String getAllCafeOfOwner(Model model){
        List<CafeAdditionDto> cafes = ownerService.getAllCafeOfOwner();
        model.addAttribute("cafes", cafes);
        return "owner/cafeList";
    }

    @GetMapping("/pricing/{cafeId}")
    public String showPricingSettings(@PathVariable("cafeId") long cafeId, Model model) {
        PricingRuleDto pricingRule = pricingRuleService.getRuleDto(cafeId);
        List<OfflineBooking> recentOfflineBookings = offlineBookingService.getRecentBookings(cafeId);
        model.addAttribute("cafeId", cafeId);
        model.addAttribute("pricingRule", pricingRule);
        model.addAttribute("offlineBookingDto", new OfflineBookingDto());
        model.addAttribute("recentOfflineBookings", recentOfflineBookings);
        return "owner/pricingSettings";
    }

    @PostMapping("/pricing/{cafeId}")
    public String updatePricingSettings(@PathVariable("cafeId") long cafeId,
                                        @ModelAttribute("pricingRule") PricingRuleDto pricingRuleDto) {
        pricingRuleService.updateRule(cafeId, pricingRuleDto);
        return "redirect:/owner/pricing/" + cafeId;
    }

    @PostMapping("/pricing/{cafeId}/offline-bookings")
    public String addOfflineBooking(@PathVariable("cafeId") long cafeId,
                                    @ModelAttribute("offlineBookingDto") OfflineBookingDto offlineBookingDto) {
        offlineBookingService.addOfflineBooking(cafeId, offlineBookingDto);
        return "redirect:/owner/pricing/" + cafeId;
    }

    @GetMapping("/addSeat/{cafeId}")
    public String showAddPCForm(@PathVariable("cafeId") long cafeId, Model model){
        model.addAttribute("cafeId", cafeId);
        model.addAttribute("pcDto", new PCDto());
        return "owner/addPc";
    }

    @PostMapping("/addSeat/{cafeId}")
    public String addPC(@PathVariable("cafeId") long cafeId, @ModelAttribute("pcDto") PCDto pcd){
        ownerService.addPC(cafeId, pcd);
        return "redirect:/owner/PCs/" + cafeId;
    }

    @GetMapping("/PCs/{cafeId}")
    public String getAllPcOfCafe(@PathVariable("cafeId") long cafeId, Model model){
        List<PCDto> pcs = ownerService.getAllPcOfCafe(cafeId);
        model.addAttribute("pcs", pcs);
        model.addAttribute("cafeId", cafeId);
        return "owner/pcList";
    }

    @GetMapping("/slots/{pcId}")
    public String showSlotList(@PathVariable("pcId") long pcId,
                               @RequestParam(value = "date", required = false) LocalDate date,
                               Model model) {
        LocalDate selectedDate = date != null ? date : LocalDate.now();
        List<SlotDetails> slots = ownerService.getSlotsForPC(pcId, selectedDate);
        PC pc = ownerService.getPCById(pcId);

        model.addAttribute("slots", slots);
        model.addAttribute("pcId", pcId);
        model.addAttribute("selectedDate", selectedDate);
        model.addAttribute("minimumBookingDate", LocalDate.now());
        if (pc != null && pc.getCafe() != null) {
            model.addAttribute("cafeId", pc.getCafe().getId());
        } else {
            model.addAttribute("cafeId", 0);
        }
        return "owner/slotList";
    }

    @GetMapping("/addSlots/{pcId}")
    public String showAddSlotsForm(@PathVariable("pcId") long pcId, Model model){
        model.addAttribute("slotDto", new SlotDto());
        model.addAttribute("pcId", pcId);
        return "owner/addSlots";
    }

    @PostMapping("/addSlots/{pcId}")
    public String addSlots(@PathVariable("pcId") long pcId, @ModelAttribute("slotDto") SlotDto slotDto){
        slotDto.setPcId(pcId);
        ownerService.addSlots(slotDto);
        return "redirect:/owner/slots/" + pcId;
    }

    @PostMapping("/slots/{pcId}/edit/{slotId}")
    public String editSlot(@PathVariable("pcId") long pcId,
                           @PathVariable("slotId") long slotId,
                           @RequestParam("startTime") LocalTime startTime,
                           @RequestParam(value = "date", required = false) LocalDate date) {
        ownerService.updateSlot(slotId, startTime);
        return date == null
                ? "redirect:/owner/slots/" + pcId
                : "redirect:/owner/slots/" + pcId + "?date=" + date;
    }

    @PostMapping("/slots/{pcId}/delete/{slotId}")
    public String deleteSlot(@PathVariable("pcId") long pcId,
                             @PathVariable("slotId") long slotId,
                             @RequestParam(value = "date", required = false) LocalDate date) {
        ownerService.deleteSlot(slotId);
        return date == null
                ? "redirect:/owner/slots/" + pcId
                : "redirect:/owner/slots/" + pcId + "?date=" + date;
    }

    @GetMapping("/analytics/popular-games/{cafeId}")
    public String showPopularGamesAnalytics(@PathVariable("cafeId") long cafeId, Model model) {
        model.addAttribute("cafeId", cafeId);
        return "owner/analytics";
    }

    @GetMapping("/analytics/popular-games")
    public String redirectPopularGamesAnalytics() {
        return "redirect:/owner/";
    }

    // NEW METHOD to handle the new sidebar link
    @GetMapping("/analytics/monthly-bookings/{cafeId}")
    public String showMonthlyBookingAnalytics(@PathVariable("cafeId") long cafeId, Model model) {
        model.addAttribute("cafeId", cafeId);
        // This returns the SAME analytics page, which now contains both charts.
        return "owner/analytics";
    }

    @GetMapping("/analytics/monthly-bookings")
    public String redirectMonthlyBookingAnalytics() {
        return "redirect:/owner/";
    }
}
