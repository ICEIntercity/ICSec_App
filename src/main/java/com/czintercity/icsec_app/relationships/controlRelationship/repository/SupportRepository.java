package com.czintercity.icsec_app.relationships.controlRelationship.repository;

import com.czintercity.icsec_app.relationships.controlRelationship.entity.Synergy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
/**
 * Spring Data repository for {@link com.czintercity.icsec_app.relationships.controlRelationship.entity.Support} relationships.
 */
public interface SupportRepository extends CrudRepository<Synergy, UUID> {
    /** Returns {@code true} if a Support relationship already exists between the given source and target controls. */
    boolean existsBySource_IdAndTarget_Id(UUID sourceId, UUID targetId);
}
