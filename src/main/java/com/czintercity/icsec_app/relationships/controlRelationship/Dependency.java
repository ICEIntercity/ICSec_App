package com.czintercity.icsec_app.relationships.controlRelationship;


import com.czintercity.icsec_app.relationships.controlRelationship.record.ControlRelationshipVisuals;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Transient;

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

    @Transient
    @Override
    public ControlRelationshipType getType() {
        return ControlRelationshipType.DEPENDENCY;
    }

    @Transient
    @Override
    public ControlRelationshipVisuals getVisuals() {
        return new ControlRelationshipVisuals("#dc3545", false, "to");
    }

    public Dependency(){
        super();
    }

}
