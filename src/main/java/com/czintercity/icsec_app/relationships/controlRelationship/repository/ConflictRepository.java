package com.czintercity.icsec_app.relationships.controlRelationship.repository;

import com.czintercity.icsec_app.relationships.controlRelationship.entity.Synergy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
/**
 * Spring Data repository for {@link com.czintercity.icsec_app.relationships.controlRelationship.entity.Conflict} relationships.
 */
public interface ConflictRepository extends CrudRepository<Synergy, UUID> {
    /** Returns {@code true} if a Conflict relationship already exists between the given source and target controls. */
    boolean existsBySource_IdAndTarget_Id(UUID sourceId, UUID targetId);
}
