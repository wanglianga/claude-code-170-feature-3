package com.community.eldercare.repo;

import com.community.eldercare.domain.PlanServiceItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlanServiceItemRepository extends JpaRepository<PlanServiceItem, Long> {
    List<PlanServiceItem> findByPlanIdOrderByIdAsc(Long planId);
}
