package com.community.eldercare.repo;

import com.community.eldercare.domain.PlanLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlanLogRepository extends JpaRepository<PlanLog, Long> {
    List<PlanLog> findByPlanIdOrderByCreatedAtAsc(Long planId);
}
