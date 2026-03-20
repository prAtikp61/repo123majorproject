package com.Major.majorProject.repository;

import com.Major.majorProject.entity.UserGamePreference;
import com.Major.majorProject.dto.GameCountDto; // Make sure to import the DTO
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserGamePreferenceRepository extends JpaRepository<UserGamePreference, Long> {

    @Query("SELECT new com.Major.majorProject.dto.GameCountDto(p.gameName, COUNT(p.gameName)) FROM UserGamePreference p WHERE p.cafe.id = :cafeId GROUP BY p.gameName ORDER BY COUNT(p.gameName) DESC")
    List<GameCountDto> findMostPreferredGamesByCafeId(Long cafeId);
}