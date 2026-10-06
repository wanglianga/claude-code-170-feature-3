package com.community.eldercare.repo;

import com.community.eldercare.domain.CareActivity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface CareActivityRepository extends JpaRepository<CareActivity, Long> {
    List<CareActivity> findByPlanIdOrderByActivityAtAsc(Long planId);

    List<CareActivity> findByPlanIdAndActivityTypeOrderByActivityAtAsc(Long planId, String activityType);

    long countByPlanIdAndActivityType(Long planId, String activityType);

    List<CareActivity> findByPlanIdAndActivityAtBetweenOrderByActivityAtAsc(Long planId, LocalDateTime from, LocalDateTime to);
}
