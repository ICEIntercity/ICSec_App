package com.czintercity.icsec_app.assessment.entity;

import com.czintercity.icsec_app.controls.entity.Control;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.util.UUID;

/**
 * Records the maturity and scope ratings given to a specific
 * {@link com.czintercity.icsec_app.controls.entity.Control} within a single {@link Assessment}.
 * <p>
 * Both scores use a 0–5 scale and feed directly into the coverage calculation formula.
 * A status where both scores are zero and the note is empty is considered blank and is
 * excluded from persistence.
 *
 * @see com.czintercity.icsec_app.assessment.service.CoverageCalculationService
 */
@Entity
public class ControlStatus {
    /**
     * Unique ID of the status object
     */
    @Id
    @Column(nullable = false)
    private UUID id;

    /**
     * The {@link Control} pertaining to this status object
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(referencedColumnName = "id", nullable = false)
    private Control control;

    /**
     * Coverage maturity score, from 0 to 5.
     */
    @Column(nullable = false)
    @Min(0)
    @Max(5)
    private Short coverageMaturity;

    /**
     * Coverage scope, from 0 to 5.
     */
    @Column(nullable = false)
    @Min(0)
    @Max(5)
    private Short coverageScope;

    /**
     * An optional note to store additional observations/information about the coverage.
     */
    @Column(nullable = true)
    private String note;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="assessment", referencedColumnName = "id", nullable = false)
    private Assessment assessment;

    public ControlStatus() {
        this.id = UUID.randomUUID();
    }

    public ControlStatus(Assessment assessment, Control control, Short coverageMaturity, Short coverageScope) {
        this.id = UUID.randomUUID();
        this.control = control;
        this.coverageMaturity = coverageMaturity;
        this.coverageScope = coverageScope;
        this.assessment = assessment;
    }

    // GETTERS
    public UUID getId() { return this.id;}
    public Control getControl() { return this.control; }
    public Short getCoverageMaturity() { return this.coverageMaturity; }
    public Short getCoverageScope() { return this.coverageScope; }
    public String getNote() { return this.note; }
    public Assessment getAssessment() { return this.assessment; }

    // SETTERS
    public void setControl(Control control) { this.control = control; }
    public void setCoverageMaturity(Short maturity) { this.coverageMaturity = maturity; }
    public void setCoverageScope(Short scale) { this.coverageScope = scale; }
    public void setNote(String note) { this.note = note; }
    public void setAssessment(Assessment assessment) { this.assessment = assessment; }

    public boolean isBlank() {
        return this.coverageMaturity == 0 && this.coverageScope == 0 && (this.note == null || this.note.isEmpty());
    }

    @Override
    public int hashCode(){
        return id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ControlStatus && id.equals(((ControlStatus) obj).id);
    }
}
