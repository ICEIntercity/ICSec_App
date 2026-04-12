package com.czintercity.icsec_app.relationships.controlRelationship.entity;

import com.czintercity.icsec_app.relationships.controlRelationship.ControlRelationshipType;
import com.czintercity.icsec_app.relationships.controlRelationship.record.ControlRelationshipVisuals;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Transient;

@Entity
@DiscriminatorValue("SUPPORT")
public class Support extends ControlRelationship {
    private static final String displayName = "Support";
    private static final String outboundName = "Supports";
    private static final String inboundName = "Supported by";

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
        return ControlRelationshipType.SUPPORT;
    }

    public Support(){
        super();
    }
}
