package com.czintercity.icsec_app.relationships.techniqueCoverage;

/**
 * Classifies the mechanism by which a security control addresses a MITRE ATT&CK for ICS technique.
 *
 * <ul>
 *   <li>{@link #DETERRENT} — discourages an attacker from attempting the technique.</li>
 *   <li>{@link #PREVENTATIVE} — blocks or stops the technique from succeeding.</li>
 *   <li>{@link #DETECTIVE} — identifies or alerts on the use of the technique.</li>
 *   <li>{@link #RECOVERY} — restores normal operation after the technique has been executed.</li>
 *   <li>{@link #CONTAINMENT} — limits the blast radius or lateral spread of the technique.</li>
 *   <li>{@link #UNKNOWN} — fallback used when the coverage type cannot be determined.</li>
 * </ul>
 */
public enum CoverageType {
    /** Discourages an attacker from attempting the technique. */
    DETERRENT("Deterrent", "#F59E0B"),
    /** Blocks or stops the technique from succeeding. */
    PREVENTATIVE("Preventative", "#EF4444"),
    /** Identifies or alerts on the use of the technique. */
    DETECTIVE("Detective", "#3B82F6"),
    /** Restores normal operation after the technique has been executed. */
    RECOVERY("Recovery", "#10B981"),
    /** Limits the blast radius or lateral spread of the technique. */
    CONTAINMENT("Containment", "#8B5CF6"),
    /** Fallback used when the coverage type cannot be determined. */
    UNKNOWN("Unknown", "#6B7280");

    private final String displayValue;
    private final String hexColor;

    CoverageType(String displayValue, String hexColor) {
        this.displayValue = displayValue;
        this.hexColor = hexColor;
    }

    public String getDisplayValue() {
        return displayValue;
    }

    public String getHexColor() {
        return hexColor;
    }
}
