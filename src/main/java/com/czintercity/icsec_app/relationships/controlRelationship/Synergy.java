package com.czintercity.icsec_app.relationships.controlRelationship;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("SYNERGY")
public class Synergy extends ControlRelationship {
    private static final String displayName = "Synergy";
    private static final String outboundName = "Strengthens";
    private static final String inboundName = "Strengthened by";

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

    @Override
    public ControlRelationshipType getType() {
        return ControlRelationshipType.SYNERGY;
    }

    public Synergy(){
        super();
    }
}
