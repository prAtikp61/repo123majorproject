// package com.Major.majorProject.controller;

// import com.Major.majorProject.dto.SlotDto;
// import com.Major.majorProject.service.SlotService;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.PathVariable;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RestController;

// import java.util.List;

// @RestController
// @RequestMapping("/allSlots") // Base URL for all endpoints in this controller
// public class SlotController {

//     private final SlotService slotService;

//     @Autowired
//     public SlotController(SlotService slotService) {
//         this.slotService = slotService;
//     }

//     @GetMapping("/pc/{pcId}")
//     public ResponseEntity<List<SlotDto>> findSlotsByPcId(@PathVariable Long pcId) {
//         // The error happens here if the method is not defined in SlotService
//         List<SlotDto> slots = slotService.getSlotsByPcId(pcId);
//         return ResponseEntity.ok(slots);
//     }
// }



package com.Major.majorProject.controller;

import com.Major.majorProject.dto.SlotDto;
import com.Major.majorProject.service.SlotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping("/allSlots")
public class SlotController {

    private final SlotService slotService;

    @Autowired
    public SlotController(SlotService slotService) {
        this.slotService = slotService;
    }

    // Main endpoint - returns HTML page
    @GetMapping("/pc/{pcId}")
    public String findSlotsByPcId(@PathVariable Long pcId, Model model) {
        List<SlotDto> slots = slotService.getSlotsByPcId(pcId);
        model.addAttribute("slots", slots);
        model.addAttribute("pcId", pcId);
        return "user/userSlotList"; // Remove .html extension - Thymeleaf adds it automatically
    }

    // Optional: Keep JSON API endpoint if needed elsewhere
    @GetMapping("/api/pc/{pcId}")
    @ResponseBody
    public ResponseEntity<List<SlotDto>> findSlotsByPcIdApi(@PathVariable Long pcId) {
        List<SlotDto> slots = slotService.getSlotsByPcId(pcId);
        return ResponseEntity.ok(slots);
    }
}