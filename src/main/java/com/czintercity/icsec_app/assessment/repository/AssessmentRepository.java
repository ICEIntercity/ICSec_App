package com.czintercity.icsec_app.assessment.repository;

import com.czintercity.icsec_app.assessment.entity.Assessment;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface AssessmentRepository extends CrudRepository<Assessment, UUID> {
}
