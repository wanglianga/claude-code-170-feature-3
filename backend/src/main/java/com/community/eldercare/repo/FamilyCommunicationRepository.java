package com.community.eldercare.repo;

import com.community.eldercare.domain.FamilyCommunication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface FamilyCommunicationRepository extends JpaRepository<FamilyCommunication, Long> {
    List<FamilyCommunication> findByEventIdOrderByCommunicatedAtAsc(Long eventId);

    @Query("select c from FamilyCommunication c where c.event.elder.id = :elderId and c.communicatedAt >= :since order by c.communicatedAt desc")
    List<FamilyCommunication> findByElderSince(@Param("elderId") Long elderId, @Param("since") LocalDateTime since);
}
