package com.community.eldercare.repo;

import com.community.eldercare.domain.CareAssessment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CareAssessmentRepository extends JpaRepository<CareAssessment, Long> {
    List<CareAssessment> findByElderIdOrderByAssessedAtDesc(Long elderId);
}
