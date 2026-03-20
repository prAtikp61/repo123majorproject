package com.Major.majorProject.repository;

import com.Major.majorProject.entity.CafeOwner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CafeOwnerRepository extends JpaRepository<CafeOwner,Long> {
    // This will find a CafeOwner by their unique email address
    Optional<CafeOwner> findByEmail(String email);
}
