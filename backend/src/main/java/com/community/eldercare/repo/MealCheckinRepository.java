package com.community.eldercare.repo;

import com.community.eldercare.domain.MealCheckin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface MealCheckinRepository extends JpaRepository<MealCheckin, Long> {
    List<MealCheckin> findByElderIdOrderByMealDateDescIdDesc(Long elderId);
    List<MealCheckin> findByPlanIdOrderByMealDateDescIdDesc(Long planId);
    long countByElderIdAndCheckedInTrueAndCheckedAtBetween(Long elderId, LocalDateTime from, LocalDateTime to);
}
