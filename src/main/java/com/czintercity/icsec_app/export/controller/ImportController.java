package com.czintercity.icsec_app.export.controller;

import com.czintercity.icsec_app.relationships.controlRelationship.ControlRelationshipService;
import com.czintercity.icsec_app.relationships.controlRelationship.dto.ControlRelationshipDTO;
import com.czintercity.icsec_app.relationships.controlRelationship.dto.RelationshipImportResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * Controller exposing the bulk data import UI and endpoints.
 */
@Controller
public class ImportController {
    private static final Logger log = LoggerFactory.getLogger(ImportController.class);

    private final ControlRelationshipService controlRelationshipService;

    public ImportController(ControlRelationshipService controlRelationshipService) {
        this.controlRelationshipService = controlRelationshipService;
    }

    /**
     * Renders the (unlinked) bulk relationship import page, where a JSON file can be uploaded.
     *
     * @return the import page view name
     */
    @GetMapping("/import/relationships")
    public String importRelationshipsPage() {
        log.trace("importRelationshipsPage called.");
        return "import/importRelationships";
    }

    /**
     * Bulk-imports control relationships from a JSON array. Each element carries the source and target
     * control IDs ({@code sourceId}, {@code targetId}) and the relationship {@code type}. Bad rows
     * (invalid, missing controls, or duplicates) are skipped and reported in the response.
     *
     * @param relationships JSON array of relationships to create
     * @return a summary of how many relationships were imported and why any were skipped
     */
    @PostMapping(
            value = "/import/relationships",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<RelationshipImportResult> importRelationships(@RequestBody List<ControlRelationshipDTO> relationships) {
        log.info("Bulk relationship import requested for {} relationship(s).", relationships.size());
        RelationshipImportResult result = controlRelationshipService.importRelationships(relationships);
        log.info("Relationship import complete: {} imported, {} skipped.", result.imported(), result.skipped());
        return ResponseEntity.ok(result);
    }
}