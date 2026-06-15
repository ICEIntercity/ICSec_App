package com.czintercity.icsec_app.export.dto;

import java.util.UUID;

/**
 * Export representation of the MITRE ATT&amp;CK {@link com.czintercity.icsec_app.attack.entity.Technique}
 * referenced by a technique coverage mapping.
 */
public record TechniqueExportDto(
        UUID id,
        String mitreId,
        String name
) {
}