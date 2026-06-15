package com.czintercity.icsec_app.export.dto;

import com.czintercity.icsec_app.relationships.controlRelationship.ControlRelationshipType;

import java.util.UUID;

/**
 * Export representation of a directed {@link com.czintercity.icsec_app.relationships.controlRelationship.entity.ControlRelationship}.
 * Each relationship is exported once, under its source control's {@code relationships} list.
 */
public record RelationshipExportDto(
        UUID id,
        ControlRelationshipType type,
        UUID sourceId,
        UUID targetId
) {
}