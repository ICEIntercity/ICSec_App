package com.czintercity.icsec_app.assessment.model;

/**
 * Value object holding the computed coverage statistics for a single technique within an assessment.
 * Stores optimum and effective failure probabilities derived from control maturity and scope ratings,
 * and the user-assigned priority weight.
 * Coverage scores on the 0–5 scale are derived from the failure probabilities on demand.
 */
public class AssessmentValues {
    private static final short MAX_COVERAGE_SCORE = 5;

    private Double optimumFailureProbability;
    private Double effectiveFailureProbability;
    private Double priority;

    public AssessmentValues(){
        optimumFailureProbability = 0.0;
        effectiveFailureProbability = 0.0;
        priority = 0.0;
    }

    public AssessmentValues(double optimumFailureProbability, double effectiveFailureProbability, double priorityWeight){
        this.optimumFailureProbability = optimumFailureProbability;
        this.effectiveFailureProbability = effectiveFailureProbability;
        this.priority = priorityWeight;
    }

    public Double getWeightedPriority(){
        // Calculate the difference between current and optimum coverage
        Double failureProbabilityDelta = Math.abs(effectiveFailureProbability - optimumFailureProbability);
        return failureProbabilityDelta * priority;
    }

    // SETTERS
    public void setOptimumFailureProbability(double optimumFailureProbability){ this.optimumFailureProbability = optimumFailureProbability; }
    public void setEffectiveFailureProbability(double effectiveFailureProbability){ this.effectiveFailureProbability = effectiveFailureProbability; }
    public void setPriority(double priority){ this.priority = priority; }

    // GETTERS
    public Double getOptimumFailureProbability(){ return optimumFailureProbability; }
    public Double getEffectiveFailureProbability(){ return effectiveFailureProbability; }
    public Double getPriority(){ return priority; }
    public Double getEffectiveCoverageScore(){
        return (1 - effectiveFailureProbability) * MAX_COVERAGE_SCORE;
    }
    public Double getOptimumCoverageScore(){
        return (1 - optimumFailureProbability) * MAX_COVERAGE_SCORE;
    }
}