package com.community.eldercare.repo;

import com.community.eldercare.domain.CarePlanItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CarePlanItemRepository extends JpaRepository<CarePlanItem, Long> {
    List<CarePlanItem> findByPlanIdOrderByIdAsc(Long planId);
}
