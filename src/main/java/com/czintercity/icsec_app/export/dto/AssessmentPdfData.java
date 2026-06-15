package com.czintercity.icsec_app.export.dto;

import java.util.List;

/**
 * View model bundling every element of a completed assessment for rendering to PDF by
 * {@link com.czintercity.icsec_app.export.service.AssessmentPdfService}.
 * <p>
 * All nested types expose JavaBean getters so they can be addressed directly from the Thymeleaf
 * print template, mirroring the four views of an assessment: the control assessment, the assigned
 * technique priorities, the per-coverage-type technique prioritisation, and the control priorities.
 */
public class AssessmentPdfData {

    private final String name;
    private final String description;
    private final String generatedOn;
    private final List<TopicControlGroup> controlAssessment;
    private final List<TechniquePriorityRow> techniquePriorities;
    private final List<CoverageSection> coverageSections;
    private final List<ControlPriorityRow> controlPriorities;

    public AssessmentPdfData(String name, String description, String generatedOn,
                             List<TopicControlGroup> controlAssessment,
                             List<TechniquePriorityRow> techniquePriorities,
                             List<CoverageSection> coverageSections,
                             List<ControlPriorityRow> controlPriorities) {
        this.name = name;
        this.description = description;
        this.generatedOn = generatedOn;
        this.controlAssessment = controlAssessment;
        this.techniquePriorities = techniquePriorities;
        this.coverageSections = coverageSections;
        this.controlPriorities = controlPriorities;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getGeneratedOn() { return generatedOn; }
    public List<TopicControlGroup> getControlAssessment() { return controlAssessment; }
    public List<TechniquePriorityRow> getTechniquePriorities() { return techniquePriorities; }
    public List<CoverageSection> getCoverageSections() { return coverageSections; }
    public List<ControlPriorityRow> getControlPriorities() { return controlPriorities; }

    /** A topic and the assessed controls it groups, for the control-assessment section. */
    public static class TopicControlGroup {
        private final String topicName;
        private final String topicColor;
        private final List<ControlAssessmentRow> controls;

        public TopicControlGroup(String topicName, String topicColor, List<ControlAssessmentRow> controls) {
            this.topicName = topicName;
            this.topicColor = topicColor;
            this.controls = controls;
        }

        public String getTopicName() { return topicName; }
        public String getTopicColor() { return topicColor; }
        public List<ControlAssessmentRow> getControls() { return controls; }
    }

    /** A single control's recorded maturity and scope within the assessment. */
    public static class ControlAssessmentRow {
        private final String code;
        private final String name;
        private final int maturity;
        private final int scope;

        public ControlAssessmentRow(String code, String name, int maturity, int scope) {
            this.code = code;
            this.name = name;
            this.maturity = maturity;
            this.scope = scope;
        }

        public String getCode() { return code; }
        public String getName() { return name; }
        public int getMaturity() { return maturity; }
        public int getScope() { return scope; }
    }

    /** A technique and the priority weight the user assigned to it. */
    public static class TechniquePriorityRow {
        private final String label;
        private final int priority;

        public TechniquePriorityRow(String label, int priority) {
            this.label = label;
            this.priority = priority;
        }

        public String getLabel() { return label; }
        public int getPriority() { return priority; }
    }

    /** All technique prioritisation rows for a single coverage type, grouped by tactic. */
    public static class CoverageSection {
        private final String typeName;
        private final String typeColor;
        private final List<CoverageTacticGroup> tactics;

        public CoverageSection(String typeName, String typeColor, List<CoverageTacticGroup> tactics) {
            this.typeName = typeName;
            this.typeColor = typeColor;
            this.tactics = tactics;
        }

        public String getTypeName() { return typeName; }
        public String getTypeColor() { return typeColor; }
        public List<CoverageTacticGroup> getTactics() { return tactics; }
    }

    /** A tactic and its techniques' coverage scores for one coverage type. */
    public static class CoverageTacticGroup {
        private final String tacticName;
        private final List<CoverageTechniqueRow> techniques;

        public CoverageTacticGroup(String tacticName, List<CoverageTechniqueRow> techniques) {
            this.tacticName = tacticName;
            this.techniques = techniques;
        }

        public String getTacticName() { return tacticName; }
        public List<CoverageTechniqueRow> getTechniques() { return techniques; }
    }

    /** A technique's effective coverage, achievable optimum, and weighted priority for one coverage type. */
    public static class CoverageTechniqueRow {
        private final String label;
        private final String effectiveScore;
        private final String optimumScore;
        private final String weightedPriority;

        public CoverageTechniqueRow(String label, String effectiveScore, String optimumScore, String weightedPriority) {
            this.label = label;
            this.effectiveScore = effectiveScore;
            this.optimumScore = optimumScore;
            this.weightedPriority = weightedPriority;
        }

        public String getLabel() { return label; }
        public String getEffectiveScore() { return effectiveScore; }
        public String getOptimumScore() { return optimumScore; }
        public String getWeightedPriority() { return weightedPriority; }
    }

    /** A control ranked by the priority-weighted risk reduction achievable by improving it. */
    public static class ControlPriorityRow {
        private final int rank;
        private final String code;
        private final String name;
        private final String topicName;
        private final String topicColor;
        private final String advice;
        private final String improvement;

        public ControlPriorityRow(int rank, String code, String name, String topicName, String topicColor,
                                  String advice, String improvement) {
            this.rank = rank;
            this.code = code;
            this.name = name;
            this.topicName = topicName;
            this.topicColor = topicColor;
            this.advice = advice;
            this.improvement = improvement;
        }

        public int getRank() { return rank; }
        public String getCode() { return code; }
        public String getName() { return name; }
        public String getTopicName() { return topicName; }
        public String getTopicColor() { return topicColor; }
        public String getAdvice() { return advice; }
        public String getImprovement() { return improvement; }
    }
}