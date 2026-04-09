package com.czintercity.icsec_app.relationships.controlRelationship;


import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("DEPENDENCY")
public class Dependency extends ControlRelationship {

    private static final String displayName = "dependency";
    private static final String outboundName = "requires";
    private static final String inboundName = "required by";

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
        return ControlRelationshipType.DEPENDENCY;
    }

    public Dependency(){
        super();
    }

}
