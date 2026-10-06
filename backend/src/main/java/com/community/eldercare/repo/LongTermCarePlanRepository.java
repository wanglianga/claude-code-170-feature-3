package com.community.eldercare.repo;

import com.community.eldercare.domain.LongTermCarePlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LongTermCarePlanRepository extends JpaRepository<LongTermCarePlan, Long> {
    List<LongTermCarePlan> findByElderIdOrderByCreatedAtDesc(Long elderId);
    Optional<LongTermCarePlan> findFirstByElderIdAndStatusOrderByCreatedAtDesc(Long elderId, String status);
    List<LongTermCarePlan> findByStatusOrderByCreatedAtDesc(String status);
}
