package com.czintercity.icsec_app.relationships.controlRelationship.entity;

import com.czintercity.icsec_app.relationships.controlRelationship.ControlRelationshipType;
import com.czintercity.icsec_app.relationships.controlRelationship.record.ControlRelationshipVisuals;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Transient;

@Entity
@DiscriminatorValue("COMPENSATION")
public class Compensation extends ControlRelationship {
    private static final String displayName = "Compensation";
    private static final String outboundName = "Compensates for";
    private static final String inboundName = "Compensated by";

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
        return ControlRelationshipType.COMPENSATION;
    }

    public Compensation() {
        super();
    }
}
