package com.community.eldercare.repo;

import com.community.eldercare.domain.CaseConference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CaseConferenceRepository extends JpaRepository<CaseConference, Long> {
    List<CaseConference> findByElderIdOrderByHeldAtDesc(Long elderId);
    List<CaseConference> findByPlanIdOrderByHeldAtDesc(Long planId);
}
