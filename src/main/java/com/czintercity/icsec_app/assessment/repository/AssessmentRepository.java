package com.czintercity.icsec_app.assessment.repository;

import com.czintercity.icsec_app.assessment.entity.Assessment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/** Spring Data repository for {@link Assessment} entities, keyed by {@link java.util.UUID}. */
public interface AssessmentRepository extends JpaRepository<Assessment, UUID> {

    /** Returns assessments sorted by last-updated timestamp, most recent first. */
    Page<Assessment> findAllByOrderByUpdatedDesc(Pageable pageable);
}
