package com.czintercity.icsec_app.relationships.controlRelationship.dto;

import java.util.List;

/**
 * Summary of a bulk control-relationship import: how many relationships were persisted and a
 * human-readable reason for every row that was skipped.
 *
 * @param imported number of relationships successfully created
 * @param errors   per-row messages describing why a relationship was skipped (one entry per skipped row)
 */
public record RelationshipImportResult(
        int imported,
        List<String> errors
) {
    /** Number of rows that were skipped. */
    public int skipped() {
        return errors.size();
    }
}