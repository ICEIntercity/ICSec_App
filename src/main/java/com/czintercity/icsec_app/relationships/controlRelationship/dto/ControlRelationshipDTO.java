package com.czintercity.icsec_app.relationships.controlRelationship.dto;

import com.czintercity.icsec_app.relationships.controlRelationship.entity.ControlRelationship;
import com.czintercity.icsec_app.relationships.controlRelationship.ControlRelationshipType;

import java.util.UUID;

public class ControlRelationshipDTO {

    private UUID sourceId;
    private UUID targetId;
    private ControlRelationshipType type;

    private String targetName;
    private String targetCode;


    public void setSourceId(UUID sourceId) { this.sourceId = sourceId; }
    public void setTargetId(UUID targetId) { this.targetId = targetId; }
    public void setType(ControlRelationshipType type) { this.type = type; }
    public void setTargetName(String targetName) { this.targetName = targetName; }
    public void setTargetCode(String targetCode) { this.targetCode = targetCode; }

    public UUID getSourceId() { return sourceId; }
    public UUID getTargetId() { return targetId; }
    public ControlRelationshipType getType() { return type; }
    public String getTargetName() { return targetName; }
    public String getTargetCode() { return targetCode; }

    public ControlRelationshipDTO(){
        this.sourceId = null;
        this.targetId = null;
        this.targetName = null;
        this.type = ControlRelationshipType.UNKNOWN;
    }

    public ControlRelationshipDTO(ControlRelationshipType type){
        this.sourceId = null;
        this.targetId = null;
        this.targetName = null;
        this.type = type;
    }

    public ControlRelationshipDTO(ControlRelationship relationship) {
        this.sourceId = relationship.getSource().getId();
        this.targetId = relationship.getTarget().getId();
        this.targetName = relationship.getTarget().getName();
        this.targetCode = relationship.getTarget().getCode();
        this.type = relationship.getType();
    }
}
