package com.czintercity.icsec_app.assessment.repository;

import com.czintercity.icsec_app.assessment.entity.ControlStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
/**
 * Spring Data repository for {@link ControlStatus} entities, keyed by {@link java.util.UUID}.
 * Used primarily to bulk-delete existing status entries before re-saving a revised assessment.
 */
public interface ControlStatusRepository extends JpaRepository<ControlStatus, UUID> {
}
