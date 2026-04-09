package com.czintercity.icsec_app.relationships.controlRelationship;

import com.czintercity.icsec_app.controls.Control;

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
}
