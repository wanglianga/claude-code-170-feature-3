package com.community.eldercare.repo;

import com.community.eldercare.domain.ConferenceParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConferenceParticipantRepository extends JpaRepository<ConferenceParticipant, Long> {
    List<ConferenceParticipant> findByConferenceIdOrderByIdAsc(Long conferenceId);
}
