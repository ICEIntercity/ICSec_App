package com.czintercity.icsec_app.assessment.repository;

import com.czintercity.icsec_app.assessment.entity.ControlStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ControlStatusRepository extends JpaRepository<ControlStatus, UUID> {
}
