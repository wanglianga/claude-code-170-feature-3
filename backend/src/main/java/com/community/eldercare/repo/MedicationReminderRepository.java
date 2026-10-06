package com.community.eldercare.repo;

import com.community.eldercare.domain.MedicationReminder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface MedicationReminderRepository extends JpaRepository<MedicationReminder, Long> {
    List<MedicationReminder> findByElderIdOrderByRemindAtDesc(Long elderId);
    List<MedicationReminder> findByPlanIdOrderByRemindAtDesc(Long planId);
    List<MedicationReminder> findByStatusOrderByRemindAtAsc(String status);
    long countByElderIdAndStatusAndRemindAtBetween(Long elderId, String status,
                                                   LocalDateTime from, LocalDateTime to);
}
