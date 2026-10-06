package com.community.eldercare.repo;

import com.community.eldercare.domain.CarePlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CarePlanRepository extends JpaRepository<CarePlan, Long> {
    List<CarePlan> findByElderIdOrderByCreatedAtDesc(Long elderId);
    List<CarePlan> findByStatusOrderByCreatedAtDesc(String status);
    List<CarePlan> findAllByOrderByCreatedAtDesc();
}
