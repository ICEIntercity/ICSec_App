package com.czintercity.icsec_app.export.service;

import com.czintercity.icsec_app.assessment.dto.AssessmentDTO;
import com.czintercity.icsec_app.assessment.dto.ControlStatusDTO;
import com.czintercity.icsec_app.assessment.dto.AssessmentResultDTO;
import com.czintercity.icsec_app.assessment.entity.Assessment;
import com.czintercity.icsec_app.assessment.model.AssessmentValues;
import com.czintercity.icsec_app.assessment.model.CoverageImprovement;
import com.czintercity.icsec_app.assessment.model.TacticAssessmentResult;
import com.czintercity.icsec_app.assessment.model.TechniqueAssessmentResult;
import com.czintercity.icsec_app.assessment.repository.AssessmentRepository;
import com.czintercity.icsec_app.assessment.service.AssessmentService;
import com.czintercity.icsec_app.assessment.service.CoverageCalculationService;
import com.czintercity.icsec_app.attack.entity.Tactic;
import com.czintercity.icsec_app.attack.entity.Technique;
import com.czintercity.icsec_app.attack.service.AttackService;
import com.czintercity.icsec_app.controls.entity.Control;
import com.czintercity.icsec_app.export.dto.AssessmentPdfData;
import com.czintercity.icsec_app.export.dto.AssessmentPdfData.ControlAssessmentRow;
import com.czintercity.icsec_app.export.dto.AssessmentPdfData.ControlPriorityRow;
import com.czintercity.icsec_app.export.dto.AssessmentPdfData.CoverageSection;
import com.czintercity.icsec_app.export.dto.AssessmentPdfData.CoverageTacticGroup;
import com.czintercity.icsec_app.export.dto.AssessmentPdfData.CoverageTechniqueRow;
import com.czintercity.icsec_app.export.dto.AssessmentPdfData.TechniquePriorityRow;
import com.czintercity.icsec_app.export.dto.AssessmentPdfData.TopicControlGroup;
import com.czintercity.icsec_app.relationships.techniqueCoverage.CoverageType;
import com.czintercity.icsec_app.topics.entity.Topic;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.jsoup.Jsoup;
import org.jsoup.helper.W3CDom;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

/**
 * Renders a completed {@link Assessment} to a single, standalone PDF document covering every element of
 * the assessment: the control assessment (per-control maturity and scope), the assigned technique
 * priorities, the per-coverage-type technique prioritisation (effective vs. optimum coverage and weighted
 * priority), and the control priorities (the risk reduction achievable by improving each control).
 * <p>
 * Coverage figures are computed with the same {@link CoverageCalculationService} and
 * {@link AssessmentService} used by the interactive views, so the export matches what the user sees
 * on screen.
 */
@Service
public class AssessmentPdfService {
    private static final Logger log = LoggerFactory.getLogger(AssessmentPdfService.class);

    private static final String PDF_TEMPLATE = "assessment/assessmentPdf";
    private static final String DEFAULT_TOPIC_COLOR = "#6c757d";
    /** Scores below this threshold are treated as zero when deciding whether a row carries information. */
    private static final double EPSILON = 0.005;

    private final AssessmentRepository assessmentRepository;
    private final AssessmentService assessmentService;
    private final CoverageCalculationService coverageCalculationService;
    private final AttackService attackService;
    private final SpringTemplateEngine templateEngine;

    public AssessmentPdfService(AssessmentRepository assessmentRepository, AssessmentService assessmentService,
                                CoverageCalculationService coverageCalculationService, AttackService attackService,
                                SpringTemplateEngine templateEngine) {
        this.assessmentRepository = assessmentRepository;
        this.assessmentService = assessmentService;
        this.coverageCalculationService = coverageCalculationService;
        this.attackService = attackService;
        this.templateEngine = templateEngine;
    }

    /**
     * Builds the full assessment report and renders it to PDF.
     *
     * @param assessmentId the id of the assessment to export
     * @return the rendered PDF as a byte array
     * @throws NoSuchElementException if no assessment with the given id exists
     */
    @Transactional(readOnly = true)
    public byte[] renderAssessmentPdf(UUID assessmentId) {
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new NoSuchElementException("Assessment with id " + assessmentId + " not found"));
        log.info("Rendering assessment PDF for {} ({}).", assessment.getName(), assessmentId);

        AssessmentPdfData data = buildData(assessment);
        return toPdf(data);
    }

    /** Assembles the view model for the four assessment sections. */
    private AssessmentPdfData buildData(Assessment assessment) {
        AssessmentDTO dto = new AssessmentDTO(assessment);
        Map<Tactic, List<Technique>> tacticsMap = attackService.getTacticsWithTechniques();

        return new AssessmentPdfData(
                assessment.getName(),
                assessment.getDescription(),
                LocalDate.now().toString(),
                buildControlAssessment(dto),
                buildTechniquePriorities(dto, tacticsMap),
                buildCoverageSections(assessment, tacticsMap),
                buildControlPriorities(dto)
        );
    }

    /** Control assessment: every assessed control grouped under its topic, with maturity and scope. */
    private List<TopicControlGroup> buildControlAssessment(AssessmentDTO dto) {
        List<TopicControlGroup> groups = new ArrayList<>();
        for (Map.Entry<Topic, Map<Control, ControlStatusDTO>> topicEntry : assessmentService.buildDisplayMap(dto).entrySet()) {
            List<ControlAssessmentRow> rows = new ArrayList<>();
            for (Map.Entry<Control, ControlStatusDTO> controlEntry : topicEntry.getValue().entrySet()) {
                ControlStatusDTO status = controlEntry.getValue();
                if (status.isBlank()) {
                    continue; // only controls actually included in the assessment
                }
                Control control = controlEntry.getKey();
                rows.add(new ControlAssessmentRow(control.getCode(), control.getName(),
                        nullToZero(status.getCoverageMaturity()), nullToZero(status.getCoverageScope())));
            }
            if (rows.isEmpty()) {
                continue;
            }
            rows.sort(Comparator.comparing(ControlAssessmentRow::getCode));
            Topic topic = topicEntry.getKey();
            groups.add(new TopicControlGroup(topic.getName(), colorOf(topic), rows));
        }
        return groups;
    }

    /** Technique priorities: techniques the user weighted, highest priority first. */
    private List<TechniquePriorityRow> buildTechniquePriorities(AssessmentDTO dto, Map<Tactic, List<Technique>> tacticsMap) {
        Map<UUID, Short> priorities = assessmentService.getTechniquePriorities(dto.getId());
        List<TechniquePriorityRow> rows = new ArrayList<>();
        for (List<Technique> techniques : tacticsMap.values()) {
            for (Technique technique : techniques) {
                Short priority = priorities.get(technique.getId());
                if (priority != null && priority > 0) {
                    rows.add(new TechniquePriorityRow(technique.getDisplayLabel(), priority));
                }
            }
        }
        rows.sort(Comparator.comparingInt(TechniquePriorityRow::getPriority).reversed()
                .thenComparing(TechniquePriorityRow::getLabel));
        return rows;
    }

    /** Technique prioritisation: effective vs. optimum coverage and weighted priority, one section per coverage type. */
    private List<CoverageSection> buildCoverageSections(Assessment assessment, Map<Tactic, List<Technique>> tacticsMap) {
        AssessmentResultDTO coverage = coverageCalculationService.calculateMitreCoverage(assessment);
        Map<Tactic, TacticAssessmentResult> scores = coverage.getCoverageScores();

        List<CoverageSection> sections = new ArrayList<>();
        for (CoverageType type : CoverageType.values()) {
            List<CoverageTacticGroup> tacticGroups = new ArrayList<>();

            for (Map.Entry<Tactic, List<Technique>> tacticEntry : tacticsMap.entrySet()) {
                TacticAssessmentResult tacticResult = scores.get(tacticEntry.getKey());
                if (tacticResult == null) {
                    continue;
                }
                List<CoverageTechniqueRow> techniqueRows = new ArrayList<>();
                for (Technique technique : tacticEntry.getValue()) {
                    TechniqueAssessmentResult techniqueResult = tacticResult.getTechniqueAssessmentResults().get(technique);
                    if (techniqueResult == null) {
                        continue;
                    }
                    AssessmentValues values = techniqueResult.getAssessmentResults().get(type);
                    double effective = values.getEffectiveCoverageScore();
                    double optimum = values.getOptimumCoverageScore();
                    double weighted = values.getWeightedPriority();

                    // Skip techniques no control addresses for this type and that carry no weighted priority.
                    if (optimum <= EPSILON && weighted <= EPSILON) {
                        continue;
                    }
                    techniqueRows.add(new CoverageTechniqueRow(technique.getDisplayLabel(),
                            format(effective), format(optimum), format(weighted)));
                }
                if (!techniqueRows.isEmpty()) {
                    tacticGroups.add(new CoverageTacticGroup(tacticEntry.getKey().getName(), techniqueRows));
                }
            }

            if (!tacticGroups.isEmpty()) {
                sections.add(new CoverageSection(type.getDisplayValue(), type.getHexColor(), tacticGroups));
            }
        }
        return sections;
    }

    /** Control priorities: controls ranked by the priority-weighted risk reduction an improvement would yield. */
    private List<ControlPriorityRow> buildControlPriorities(AssessmentDTO dto) {
        Map<Control, CoverageImprovement> improvements = assessmentService.calculateCoverageImprovements(dto);

        List<Map.Entry<Control, CoverageImprovement>> sorted = new ArrayList<>(improvements.entrySet());
        sorted.sort(Comparator.comparingDouble((Map.Entry<Control, CoverageImprovement> e) -> e.getValue().totalImprovement()).reversed());

        List<ControlPriorityRow> rows = new ArrayList<>();
        int rank = 1;
        for (Map.Entry<Control, CoverageImprovement> entry : sorted) {
            Control control = entry.getKey();
            CoverageImprovement improvement = entry.getValue();
            Topic topic = control.getTopic();
            rows.add(new ControlPriorityRow(rank++, control.getCode(), control.getName(),
                    topic.getName(), colorOf(topic),
                    improvement.advice().getAdvice(), format(improvement.totalImprovement())));
        }
        return rows;
    }

    /** Renders the print template for the given data and converts it to PDF bytes. */
    private byte[] toPdf(AssessmentPdfData data) {
        Context context = new Context();
        context.setVariable("data", data);
        String html = templateEngine.process(PDF_TEMPLATE, context);

        // Thymeleaf emits HTML5; parse it and re-serialize as well-formed XML for the PDF renderer.
        Document jsoupDoc = Jsoup.parse(html);
        jsoupDoc.outputSettings().syntax(Document.OutputSettings.Syntax.xml);
        org.w3c.dom.Document w3cDoc = new W3CDom().fromJsoup(jsoupDoc);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withW3cDocument(w3cDoc, null);
            builder.toStream(out);
            builder.run();
            return out.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to render assessment PDF for " + data.getName(), e);
        }
    }

    private static int nullToZero(Short value) {
        return value == null ? 0 : value;
    }

    private static String colorOf(Topic topic) {
        return topic.getColor() != null ? topic.getColor() : DEFAULT_TOPIC_COLOR;
    }

    /** Formats a 0–5 score to two decimal places. */
    private static String format(double value) {
        return String.format(java.util.Locale.US, "%.2f", value);
    }
}