package com.czintercity.icsec_app.relationships.controlRelationship.repository;

import com.czintercity.icsec_app.relationships.controlRelationship.entity.Redundancy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
/**
 * Spring Data repository for {@link Redundancy} relationships.
 */
public interface RedundancyRepository extends CrudRepository<Redundancy, UUID> {
    /** Returns {@code true} if a Redundancy relationship already exists between the given source and target controls. */
    boolean existsBySource_IdAndTarget_Id(UUID sourceId, UUID targetId);
}