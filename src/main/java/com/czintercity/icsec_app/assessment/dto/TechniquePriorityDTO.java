package com.czintercity.icsec_app.assessment.dto;

import java.util.UUID;

/**
 * Represents the priority score assigned to a single MITRE ATT&amp;CK technique
 * within an assessment, on a 0–5 scale where 0 means not relevant and 5 means
 * very high priority.
 *
 * @see com.czintercity.icsec_app.assessment.dto.TechniquePrioritiesFormDTO
 */
public class TechniquePriorityDTO {
    private UUID techniqueId;
    private Short priority;

    public TechniquePriorityDTO() {}

    public UUID getTechniqueId() { return techniqueId; }
    public void setTechniqueId(UUID techniqueId) { this.techniqueId = techniqueId; }
    public Short getPriority() { return priority; }
    public void setPriority(Short priority) { this.priority = priority; }
}