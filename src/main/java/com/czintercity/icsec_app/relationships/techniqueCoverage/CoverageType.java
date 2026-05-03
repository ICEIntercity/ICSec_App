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
    DETERRENT("Deterrent"),
    /** Blocks or stops the technique from succeeding. */
    PREVENTATIVE("Preventative"),
    /** Identifies or alerts on the use of the technique. */
    DETECTIVE("Detective"),
    /** Restores normal operation after the technique has been executed. */
    RECOVERY("Recovery"),
    /** Limits the blast radius or lateral spread of the technique. */
    CONTAINMENT("Containment"),
    /** Fallback used when the coverage type cannot be determined. */
    UNKNOWN("Unknown");

    private final String displayValue;
    private CoverageType(String displayValue) {
        this.displayValue = displayValue;
    }
    public String getDisplayValue() {
        return displayValue;
    }
}
