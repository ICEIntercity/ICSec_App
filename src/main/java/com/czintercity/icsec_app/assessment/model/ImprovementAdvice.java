package com.czintercity.icsec_app.assessment.model;

/**
 * Identifies which control deployment dimension yields the larger marginal coverage gain
 * when raised by a single step, and carries the human-readable advice shown in the UI badge.
 *
 * @see com.czintercity.icsec_app.assessment.service.AssessmentService#calculateCoverageImprovements
 */
public enum ImprovementAdvice {
    /** Raising the control's deployment scope gives the greater gain. */
    SCOPE("Increase coverage"),
    /** Raising the control's implementation maturity gives the greater gain. */
    MATURITY("Increase maturity"),
    /** The control is undeployed on both dimensions; deploy it by raising scope and maturity together. */
    DEPLOY_NEW("Deploy new control"),
    /** The control is already at maximum scope and maturity; no further gain is possible. */
    COMPLETED("Coverage completed.");

    private final String advice;

    ImprovementAdvice(String advice) {
        this.advice = advice;
    }

    /**
     * @return the badge text recommending which dimension to raise
     */
    public String getAdvice() {
        return advice;
    }
}