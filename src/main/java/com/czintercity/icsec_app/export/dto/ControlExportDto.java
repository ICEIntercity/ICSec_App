package com.czintercity.icsec_app.export.dto;

import java.util.List;
import java.util.UUID;

/**
 * Export representation of a {@link com.czintercity.icsec_app.controls.entity.Control}, including its
 * topic, technique coverage mappings, and outgoing relationships.
 */
public record ControlExportDto(
        UUID id,
        Long displayId,
        String code,
        String name,
        String description,
        Long costIndex,
        List<String> references,
        TopicExportDto topic,
        List<TechniqueCoverageExportDto> techniqueCoverage,
        List<RelationshipExportDto> relationships
) {
}