package com.czintercity.icsec_app.export.dto;

import com.czintercity.icsec_app.relationships.techniqueCoverage.CoverageType;

/**
 * Export representation of a {@link com.czintercity.icsec_app.relationships.techniqueCoverage.entity.TechniqueCoverage}
 * mapping between a control and a MITRE ATT&amp;CK technique.
 */
public record TechniqueCoverageExportDto(
        Long id,
        TechniqueExportDto technique,
        CoverageType coverageType,
        Short coverageRating,
        String justification
) {
}