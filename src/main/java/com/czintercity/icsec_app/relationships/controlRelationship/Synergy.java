package com.czintercity.icsec_app.relationships.controlRelationship;

import com.czintercity.icsec_app.relationships.controlRelationship.record.ControlRelationshipVisuals;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Transient;

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

    @Transient
    @Override
    public ControlRelationshipType getType() {
        return ControlRelationshipType.SYNERGY;
    }

    @Transient
    @Override
    public ControlRelationshipVisuals getVisuals() {
        return new ControlRelationshipVisuals("#198754", true, "to"); // Dashed Green
    }

    public Synergy(){
        super();
    }
}
