package com.Major.majorProject.repository;

import com.Major.majorProject.entity.PricingRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PricingRuleRepository extends JpaRepository<PricingRule, Long> {
    Optional<PricingRule> findByCafeId(Long cafeId);
}
