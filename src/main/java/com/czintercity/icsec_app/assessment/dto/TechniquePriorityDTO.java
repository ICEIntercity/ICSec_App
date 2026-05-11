package com.czintercity.icsec_app.assessment.dto;

import java.util.UUID;

public class TechniquePriorityDTO {
    private UUID techniqueId;
    private Short priority;

    public TechniquePriorityDTO() {}

    public UUID getTechniqueId() { return techniqueId; }
    public void setTechniqueId(UUID techniqueId) { this.techniqueId = techniqueId; }
    public Short getPriority() { return priority; }
    public void setPriority(Short priority) { this.priority = priority; }
}