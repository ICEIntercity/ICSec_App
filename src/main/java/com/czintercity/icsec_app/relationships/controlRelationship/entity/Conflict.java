package com.czintercity.icsec_app.relationships.controlRelationship.entity;

import com.czintercity.icsec_app.relationships.controlRelationship.ControlRelationshipType;
import com.czintercity.icsec_app.relationships.controlRelationship.record.ControlRelationshipVisuals;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Transient;

@Entity
@DiscriminatorValue("CONFLICT")
public class Conflict extends ControlRelationship {
    private static final String displayName = "Conflict";
    private static final String outboundName = "Conflicts with";
    private static final String inboundName = "Conflicts with";

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
        return ControlRelationshipType.CONFLICT;
    }

    public Conflict() {
        super();
    }
}
