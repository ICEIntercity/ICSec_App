package com.czintercity.icsec_app.export.controller;

import com.czintercity.icsec_app.export.dto.ExportDto;
import com.czintercity.icsec_app.export.service.AssessmentPdfService;
import com.czintercity.icsec_app.export.service.ControlPdfService;
import com.czintercity.icsec_app.export.service.ExportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.NoSuchElementException;
import java.util.UUID;

/**
 * REST controller exposing data export endpoints.
 */
@RestController
public class ExportController {
    private static final Logger log = LoggerFactory.getLogger(ExportController.class);

    private final ExportService exportService;
    private final ControlPdfService controlPdfService;
    private final AssessmentPdfService assessmentPdfService;

    public ExportController(ExportService exportService, ControlPdfService controlPdfService,
                            AssessmentPdfService assessmentPdfService) {
        this.exportService = exportService;
        this.controlPdfService = controlPdfService;
        this.assessmentPdfService = assessmentPdfService;
    }

    /**
     * Exports all controls and their child entities (technique coverage and relationships) as a single
     * JSON object, served as a file download.
     *
     * @return the full export as a downloadable JSON attachment
     */
    @GetMapping(value = "/export/json", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ExportDto> exportJson() {
        log.trace("exportAll called.");
        ExportDto export = exportService.exportAll();

        String filename = "icsec-export-" + LocalDate.now() + ".json";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(export);
    }

    /**
     * Exports a single control as a PDF, rendered with the standard appearance but without the
     * application navigation header, served as a file download.
     *
     * @param id the id of the control to export
     * @return the rendered PDF as a downloadable attachment
     */
    @GetMapping(value = "/export/control/{id}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> exportControlPdf(@PathVariable UUID id) {
        log.trace("exportControlPdf(id={}) called.", id);
        byte[] pdf;
        try {
            pdf = controlPdfService.renderControlPdf(id);
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }

        String filename = "control-" + id + ".pdf";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    /**
     * Exports every control as an individual PDF, bundled together into a single ZIP archive served
     * as a file download.
     *
     * @return the ZIP archive of per-control PDFs as a downloadable attachment
     */
    @GetMapping(value = "/export/controls/pdf", produces = "application/zip")
    public ResponseEntity<byte[]> exportAllControlsPdf() {
        log.trace("exportAllControlsPdf called.");
        byte[] zip = controlPdfService.renderAllControlsZip();

        String filename = "icsec-controls-" + LocalDate.now() + ".zip";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("application/zip"))
                .body(zip);
    }

    /**
     * Exports a completed assessment as a single PDF report covering every element of the assessment:
     * the control assessment, the assigned technique priorities, the technique prioritisation (coverage)
     * for all coverage types, and the control priorities.
     *
     * @param id the id of the assessment to export
     * @return the rendered PDF as a downloadable attachment
     */
    @GetMapping(value = "/export/assessment/{id}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> exportAssessmentPdf(@PathVariable UUID id) {
        log.trace("exportAssessmentPdf(id={}) called.", id);
        byte[] pdf;
        try {
            pdf = assessmentPdfService.renderAssessmentPdf(id);
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }

        String filename = "assessment-" + id + ".pdf";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}