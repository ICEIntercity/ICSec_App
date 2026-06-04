package com.czintercity.icsec_app.relationships.controlRelationship.repository;

import com.czintercity.icsec_app.relationships.controlRelationship.entity.Dependency;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
/**
 * Spring Data repository for {@link com.czintercity.icsec_app.relationships.controlRelationship.entity.Dependency} relationships.
 */
public interface DependencyRepository extends CrudRepository<Dependency, UUID> {
    /** Returns {@code true} if a Dependency relationship already exists between the given source and target controls. */
    boolean existsBySource_IdAndTarget_Id(UUID sourceId, UUID targetId);
}
