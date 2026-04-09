package com.czintercity.icsec_app.relationships.controlRelationship;

import com.czintercity.icsec_app.controls.Control;

import java.util.Objects;

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
}
