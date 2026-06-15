package com.czintercity.icsec_app.export.dto;

import java.util.UUID;

/**
 * Export representation of the {@link com.czintercity.icsec_app.topics.entity.Topic} a control belongs to.
 */
public record TopicExportDto(
        UUID id,
        String code,
        String name,
        String description,
        String color
) {
}