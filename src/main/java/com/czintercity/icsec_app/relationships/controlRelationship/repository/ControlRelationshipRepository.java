package com.czintercity.icsec_app.relationships.controlRelationship.repository;

import com.czintercity.icsec_app.relationships.controlRelationship.entity.ControlRelationship;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
/**
 * Spring Data repository for all {@link ControlRelationship} subtypes, keyed by {@link UUID}.
 * Operates on the shared single-table inheritance table and can read or delete any concrete relationship type.
 */
public interface ControlRelationshipRepository extends CrudRepository<ControlRelationship, UUID> {
}
