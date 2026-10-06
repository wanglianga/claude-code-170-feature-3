package com.community.eldercare.repo;

import com.community.eldercare.domain.PlanServiceExecution;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface PlanServiceExecutionRepository extends JpaRepository<PlanServiceExecution, Long> {
    List<PlanServiceExecution> findByPlanIdOrderByExecutedAtDesc(Long planId);
    List<PlanServiceExecution> findByElderIdOrderByExecutedAtDesc(Long elderId);
    long countByPlanIdAndServiceTypeAndExecutedAtBetween(Long planId, String serviceType,
                                                        LocalDateTime from, LocalDateTime to);
}
