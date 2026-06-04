package com.czintercity.icsec_app.relationships.controlRelationship.record;

/**
 * Immutable value object carrying the visual rendering properties for a control relationship graph edge.
 *
 * @param color     CSS colour string (e.g. {@code "#6610f2"}) for the edge
 * @param dashed    {@code true} if the edge should be rendered with a dashed line
 * @param arrowType Cytoscape/vis.js arrow-head style (e.g. {@code "to"} or {@code "none"})
 */
public record ControlRelationshipVisuals(
        String color,
        boolean dashed,
        String arrowType
){}
