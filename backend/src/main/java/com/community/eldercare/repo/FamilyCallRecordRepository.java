package com.community.eldercare.repo;

import com.community.eldercare.domain.FamilyCallRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface FamilyCallRecordRepository extends JpaRepository<FamilyCallRecord, Long> {
    List<FamilyCallRecord> findByElderIdOrderByCalledAtDesc(Long elderId);
    List<FamilyCallRecord> findByPlanIdOrderByCalledAtDesc(Long planId);
    List<FamilyCallRecord> findByElderIdAndCalledAtBetweenOrderByCalledAtDesc(Long elderId,
                                                                              LocalDateTime from, LocalDateTime to);
}
