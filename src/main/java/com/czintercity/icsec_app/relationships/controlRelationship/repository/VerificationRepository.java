package com.czintercity.icsec_app.relationships.controlRelationship.repository;

import com.czintercity.icsec_app.relationships.controlRelationship.entity.Synergy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface VerificationRepository extends CrudRepository<Synergy, UUID> {
    boolean existsBySource_IdAndTarget_Id(UUID sourceId, UUID targetId);
}
