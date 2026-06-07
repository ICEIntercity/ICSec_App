package com.czintercity.icsec_app.relationships.controlRelationship.entity;

import com.czintercity.icsec_app.relationships.controlRelationship.ControlRelationshipType;
import com.czintercity.icsec_app.relationships.controlRelationship.record.ControlRelationshipVisuals;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Transient;

/**
 * A {@link ControlRelationship} where the source control duplicates or overlaps the protection provided by the target control.
 */
@Entity
@DiscriminatorValue("REDUNDANCY")
public class Redundancy extends ControlRelationship {
    private static final String displayName = "Redundancy";
    private static final String outboundName = "Redundant with";
    private static final String inboundName = "Covered by";

    @Override
    @Transient
    public String getDisplayName() {
        return displayName;
    }

    @Override
    @Transient
    protected String getInboundName() {
        return inboundName;
    }

    @Override
    @Transient
    protected String getOutboundName() {
        return outboundName;
    }

    @Override
    @Transient
    public ControlRelationshipType getType() {
        return ControlRelationshipType.REDUNDANCY;
    }

    public Redundancy() {
        super();
    }
}