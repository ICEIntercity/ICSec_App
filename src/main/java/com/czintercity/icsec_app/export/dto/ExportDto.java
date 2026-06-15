package com.czintercity.icsec_app.export.dto;

import java.util.List;

/**
 * Root object of a full data export. Contains the export {@code date} (ISO-8601) and the
 * complete list of exported controls with their child entities.
 */
public record ExportDto(
        String date,
        List<ControlExportDto> controls
) {
}