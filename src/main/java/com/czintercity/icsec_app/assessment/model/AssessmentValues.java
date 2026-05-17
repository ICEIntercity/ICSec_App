package com.czintercity.icsec_app.assessment.model;

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