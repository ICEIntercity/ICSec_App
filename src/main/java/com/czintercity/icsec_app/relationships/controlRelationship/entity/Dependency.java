package com.czintercity.icsec_app.relationships.controlRelationship.entity;


import com.czintercity.icsec_app.relationships.controlRelationship.ControlRelationshipType;
import com.czintercity.icsec_app.relationships.controlRelationship.record.ControlRelationshipVisuals;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Transient;

/**
 * A {@link ControlRelationship} where the source control requires the target control to function correctly.
 * Rendered as "Requires" in the outbound direction and "Required by" in the inbound direction.
 */
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
