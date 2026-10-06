package com.community.eldercare.repo;

import com.community.eldercare.domain.ElderArchiveNote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ElderArchiveNoteRepository extends JpaRepository<ElderArchiveNote, Long> {
    List<ElderArchiveNote> findByElderIdOrderByCreatedAtDesc(Long elderId);
}
