package com.czintercity.icsec_app.relationships.controlRelationship.entity;


import com.czintercity.icsec_app.relationships.controlRelationship.ControlRelationshipType;
import com.czintercity.icsec_app.relationships.controlRelationship.record.ControlRelationshipVisuals;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Transient;

@Entity
@DiscriminatorValue("DEPENDENCY")
public class Dependency extends ControlRelationship {

    private static final String displayName = "Dependency";
    private static final String outboundName = "Requires";
    private static final String inboundName = "Required by";

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    protected String getOutboundName() {
        return outboundName;
    }

    @Override
    protected String getInboundName() {
        return inboundName;
    }

    @Transient
    @Override
    public ControlRelationshipType getType() {
        return ControlRelationshipType.DEPENDENCY;
    }

    public Dependency(){
        super();
    }

}
