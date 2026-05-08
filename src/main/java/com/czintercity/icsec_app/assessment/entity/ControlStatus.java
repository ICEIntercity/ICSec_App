package com.czintercity.icsec_app.assessment.entity;

import com.czintercity.icsec_app.controls.entity.Control;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.util.UUID;

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
    @OneToOne(cascade=CascadeType.REMOVE, orphanRemoval = true)
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

    public ControlStatus() {
        this.id = UUID.randomUUID();
    }

    public ControlStatus(Control control, Short coverageMaturity, Short coverageScope) {
        this.id = UUID.randomUUID();
        this.control = control;
        this.coverageMaturity = coverageMaturity;
        this.coverageScope = coverageScope;
    }

    // GETTERS
    public UUID getId() { return this.id;}
    public Control getControl() { return this.control; }
    public Short getCoverageMaturity() { return this.coverageMaturity; }
    public Short getCoverageScope() { return this.coverageScope; }
    public String getNote() { return this.note; }

    // SETTERS
    public void setControl(Control control) { this.control = control; }
    public void setCoverageMaturity(Short maturity) { this.coverageMaturity = maturity; }
    public void setCoverageScope(Short scale) { this.coverageScope = scale; }
    public void setNote(String note) { this.note = note; }

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
