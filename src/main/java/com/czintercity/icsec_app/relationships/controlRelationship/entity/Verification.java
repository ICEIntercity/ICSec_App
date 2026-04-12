package com.czintercity.icsec_app.relationships.controlRelationship.entity;

import com.czintercity.icsec_app.relationships.controlRelationship.ControlRelationshipType;
import com.czintercity.icsec_app.relationships.controlRelationship.record.ControlRelationshipVisuals;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Transient;

@Entity
@DiscriminatorValue("VERIFICATION")
public class Verification extends ControlRelationship {
    private static final String displayName = "Verification";
    private static final String outboundName = "Verifies";
    private static final String inboundName = "Verified by";

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
        return ControlRelationshipType.VERIFICATION;
    }

    public Verification() {
        super();
    }
}
