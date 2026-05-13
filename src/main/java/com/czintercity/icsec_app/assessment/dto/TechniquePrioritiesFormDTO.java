package com.czintercity.icsec_app.assessment.dto;

import java.util.List;

/**
 * Form-binding wrapper for the MITRE technique prioritisation submission.
 * Holds an ordered list of {@link TechniquePriorityDTO} entries, one per technique
 * card rendered on the prioritisation view, matching the indexed field naming
 * convention used by Thymeleaf ({@code priorities[n].techniqueId}, etc.).
 */
public class TechniquePrioritiesFormDTO {
    private List<TechniquePriorityDTO> priorities;

    public List<TechniquePriorityDTO> getPriorities() { return priorities; }
    public void setPriorities(List<TechniquePriorityDTO> priorities) { this.priorities = priorities; }
}
