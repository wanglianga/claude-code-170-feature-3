package com.community.eldercare.repo;

import com.community.eldercare.domain.OnsiteVisit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OnsiteVisitRepository extends JpaRepository<OnsiteVisit, Long> {
    List<OnsiteVisit> findByEventIdOrderByArrivedAtAsc(Long eventId);

    @Query("select v from OnsiteVisit v where v.event.elder.id = :elderId " +
            "and v.arrivedAt >= :since order by v.arrivedAt asc")
    List<OnsiteVisit> findByElderSince(@Param("elderId") Long elderId, @Param("since") LocalDateTime since);
}
