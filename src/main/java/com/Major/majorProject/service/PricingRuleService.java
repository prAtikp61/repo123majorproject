package com.Major.majorProject.service;

import com.Major.majorProject.dto.PricingRuleDto;
import com.Major.majorProject.entity.Cafe;
import com.Major.majorProject.entity.CafeOwner;
import com.Major.majorProject.entity.PricingRule;
import com.Major.majorProject.repository.CafeOwnerRepository;
import com.Major.majorProject.repository.CafeRepository;
import com.Major.majorProject.repository.PricingRuleRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class PricingRuleService {

    private final PricingRuleRepository pricingRuleRepository;
    private final CafeRepository cafeRepository;
    private final CafeOwnerRepository cafeOwnerRepository;

    public PricingRuleService(PricingRuleRepository pricingRuleRepository,
                              CafeRepository cafeRepository,
                              CafeOwnerRepository cafeOwnerRepository) {
        this.pricingRuleRepository = pricingRuleRepository;
        this.cafeRepository = cafeRepository;
        this.cafeOwnerRepository = cafeOwnerRepository;
    }

    public PricingRule getOrCreateByCafeId(Long cafeId) {
        return pricingRuleRepository.findByCafeId(cafeId)
                .orElseGet(() -> {
                    Cafe cafe = cafeRepository.findById(cafeId)
                            .orElseThrow(() -> new RuntimeException("Cafe not found"));
                    PricingRule rule = new PricingRule();
                    rule.setCafe(cafe);
                    rule.setBasePrice(cafe.getHourlyRate() != null ? cafe.getHourlyRate() : 100.0);
                    rule.setLowDemandThreshold(10);
                    rule.setHighDemandThreshold(25);
                    rule.setLowMultiplier(0.8);
                    rule.setHighMultiplier(1.5);
                    return pricingRuleRepository.save(rule);
                });
    }

    public PricingRuleDto getRuleDto(Long cafeId) {
        PricingRule rule = getOrCreateByCafeId(cafeId);
        PricingRuleDto dto = new PricingRuleDto();
        dto.setCafeId(cafeId);
        dto.setBasePrice(rule.getBasePrice());
        dto.setLowDemandThreshold(rule.getLowDemandThreshold());
        dto.setHighDemandThreshold(rule.getHighDemandThreshold());
        dto.setLowMultiplier(rule.getLowMultiplier());
        dto.setHighMultiplier(rule.getHighMultiplier());
        return dto;
    }

    @Transactional
    public void updateRule(Long cafeId, PricingRuleDto dto) {
        PricingRule rule = getOrCreateOwnedRule(cafeId);
        rule.setBasePrice(dto.getBasePrice());
        rule.setLowDemandThreshold(dto.getLowDemandThreshold());
        rule.setHighDemandThreshold(dto.getHighDemandThreshold());
        rule.setLowMultiplier(dto.getLowMultiplier());
        rule.setHighMultiplier(dto.getHighMultiplier());
        pricingRuleRepository.save(rule);
    }

    private PricingRule getOrCreateOwnedRule(Long cafeId) {
        String ownerEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        CafeOwner owner = cafeOwnerRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new RuntimeException("Owner not found"));
        Cafe cafe = cafeRepository.findById(cafeId)
                .orElseThrow(() -> new RuntimeException("Cafe not found"));
        if (cafe.getOwner() == null || !cafe.getOwner().getId().equals(owner.getId())) {
            throw new RuntimeException("You do not have access to this cafe.");
        }
        return getOrCreateByCafeId(cafeId);
    }
}
