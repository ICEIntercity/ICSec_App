package com.czintercity.icsec_app.assessment.service;

import com.czintercity.icsec_app.assessment.dto.AssessmentDTO;
import com.czintercity.icsec_app.assessment.dto.ControlStatusDTO;
import com.czintercity.icsec_app.assessment.dto.TechniquePriorityDTO;
import com.czintercity.icsec_app.assessment.entity.Assessment;
import com.czintercity.icsec_app.assessment.entity.ControlStatus;
import com.czintercity.icsec_app.assessment.repository.AssessmentRepository;
import com.czintercity.icsec_app.assessment.repository.ControlStatusRepository;
import com.czintercity.icsec_app.attack.entity.Technique;
import com.czintercity.icsec_app.attack.repository.TechniqueRepository;
import com.czintercity.icsec_app.assessment.model.ImprovementAdvice;
import com.czintercity.icsec_app.assessment.model.CoverageImprovement;
import com.czintercity.icsec_app.controls.entity.Control;
import com.czintercity.icsec_app.controls.repository.ControlRepository;
import com.czintercity.icsec_app.relationships.techniqueCoverage.CoverageType;
import com.czintercity.icsec_app.relationships.techniqueCoverage.entity.TechniqueCoverage;
import com.czintercity.icsec_app.topics.entity.Topic;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

import static com.czintercity.icsec_app.assessment.service.CoverageCalculationService.MAX_RATING;
import static com.czintercity.icsec_app.assessment.service.CoverageCalculationService.failureProbability;

/**
 * Core service for creating, loading, and persisting {@link Assessment} records.
 * <p>
 * Responsibilities include building the control-status display map used by the
 * assessment edit view, delegating to repositories for persistence, and managing
 * MITRE ATT&amp;CK technique priorities.
 */
@Service
public class AssessmentService {
    private final ControlRepository controlRepository;
    private final ControlStatusRepository controlStatusRepository;
    private final AssessmentRepository assessmentRepository;
    private final TechniqueRepository techniqueRepository;

    public AssessmentService(ControlRepository controlRepository, ControlStatusRepository controlStatusRepository,
                             AssessmentRepository assessmentRepository, TechniqueRepository techniqueRepository) {
        this.controlRepository = controlRepository;
        this.controlStatusRepository = controlStatusRepository;
        this.assessmentRepository = assessmentRepository;
        this.techniqueRepository = techniqueRepository;
    }

    /**
     * Builds a mapping between all controls and their status in a given {@link AssessmentDTO}
     * @param dto an {@link AssessmentDTO} containing the sparse map of ratings
     * @return a Map containing all available controls and the status of each
     */
    public Map<Control, ControlStatusDTO> buildControlStatusMap(AssessmentDTO dto) {
        Map<UUID, ControlStatusDTO> lookup = indexStatuses(dto, false);

        Map<Control, ControlStatusDTO> out = new HashMap<>();
        for (Control control : controlRepository.findAll()) {
            ControlStatusDTO statusDTO = lookup.get(control.getId());
            if (statusDTO != null) {
                out.put(control, statusDTO);
            } else {
                out.put(control, new ControlStatusDTO(dto.getId(), control.getId()));
            }
        }

        return out;
    }

    /**
     * Builds a two-level map of all controls and their statuses, grouped by {@link Topic}.
     * <p>
     * Intended for use in view rendering where controls must be displayed under their parent topic.
     * Controls with no existing assessment entry receive a blank {@link ControlStatus} with zero scores.
     * </p>
     *
     * @param dto the {@link AssessmentDTO} whose saved statuses should be included
     * @return a {@link LinkedHashMap} of topics to their controls and statuses; insertion order is preserved
     * @see #buildControlStatusMap(AssessmentDTO)
     */
    public Map<Topic, Map<Control, ControlStatusDTO>> buildDisplayMap(AssessmentDTO dto) {
        Map<Topic, Map<Control, ControlStatusDTO>> grouped = new LinkedHashMap<>();

        for (Map.Entry<Control, ControlStatusDTO> entry : buildControlStatusMap(dto).entrySet()) {
            Topic topic = entry.getKey().getTopic();

            // Create a bucket for the topic on first encounter
            if (!grouped.containsKey(topic)) {
                grouped.put(topic, new LinkedHashMap<>());
            }

            grouped.get(topic).put(entry.getKey(), entry.getValue());
        }

        return grouped;
    }

    /**
     * Persists an assessment from the given {@link AssessmentDTO}.
     * If an assessment with the same ID already exists it is updated in place;
     * otherwise a new record is created. All existing {@link ControlStatus} entries
     * are replaced with the ones provided in the DTO, skipping any blank entries.
     *
     * @return the saved {@link Assessment} entity
     */
    @Transactional
    public Assessment saveAssessment(AssessmentDTO dto) {
        Optional<Assessment> existing = assessmentRepository.findById(dto.getId());
        Assessment assessment;
        if (existing.isPresent()) {
            assessment = existing.get();
        } else {
            assessment = new Assessment(dto.getId());
        }

        assessment.setName(dto.getName());
        assessment.setDescription(dto.getDescription());

        if(assessment.getControlStatusMapping() != null) {
            controlStatusRepository.deleteAll(assessment.getControlStatusMapping());
        }

        List<ControlStatus> statusList = new ArrayList<>();
        if (dto.getControlStatusMapping() != null) {
            for (ControlStatusDTO statusDTO : dto.getControlStatusMapping()) {
                if (!statusDTO.isBlank()) {
                    statusList.add(new ControlStatus(assessment,
                            controlRepository.getReferenceById(statusDTO.getControlId()),
                            statusDTO.getCoverageMaturity(),
                            statusDTO.getCoverageScope()
                    ));
                }
            }
        }
        assessment.setControlStatusMapping(statusList);
        return assessmentRepository.save(assessment);
    }

    /**
     * Returns a map of technique ID to priority score for the given assessment,
     * containing only techniques that have been explicitly prioritised (score &gt; 0).
     */
    @Transactional
    public Map<UUID, Short> getTechniquePriorities(UUID assessmentId) {
        Assessment assessment = requireAssessment(assessmentId);
        Map<UUID, Short> priorities = new HashMap<>();
        if (assessment.getTechniquePriorities() != null) {
            for (Map.Entry<Technique, Short> entry : assessment.getTechniquePriorities().entrySet()) {
                priorities.put(entry.getKey().getId(), entry.getValue());
            }
        }
        return priorities;
    }

    /**
     * Saves technique priorities from a DTO for an existing {@link Assessment}.
     *
     * @param assessmentId the {@link UUID} of the {@link Assessment} object that the technique priorities belong to
     * @param priorities a {@link List} of {@link TechniquePriorityDTO} for each technique
     * @return the updated {@link Assessment} object
     */
    @Transactional
    public Assessment saveTechniquePriorities(UUID assessmentId, List<TechniquePriorityDTO> priorities) {
        Assessment assessment = requireAssessment(assessmentId);
        Map<Technique, Short> priorityMap = new HashMap<>();
        if (priorities != null) {
            for (TechniquePriorityDTO dto : priorities) {
                if (dto.getPriority() != null && dto.getPriority() > 0) {
                    priorityMap.put(techniqueRepository.getReferenceById(dto.getTechniqueId()), dto.getPriority());
                }
            }
        }
        assessment.setTechniquePriorities(priorityMap);
        return assessmentRepository.save(assessment);
    }

    /**
     * Calculates the coverage improvement achievable for each control, measured as the total
     * priority-weighted reduction in residual failure probability (the risk faced) across every technique
     * the control covers if one of its two deployment dimensions is raised by a single point. Each
     * technique's reduction is weighted by its assigned priority, mirroring the coverage view's
     * {@code weightedPriority = failureProbabilityDelta × priority}; techniques with no assigned
     * priority contribute nothing.
     * <p>
     * The residual failure probability for a technique and {@link CoverageType} is the product, over
     * all deployed controls, of each control's {@code max(0, 1 − effectiveScalingFactor × rating / 5)}
     * — the same compounding model used by {@link CoverageCalculationService#calculateMitreCoverage}.

     * Each control is scored on raising its scope by one point and on raising its maturity by one
     * point (neither beyond the maximum of {@value CoverageCalculationService#MAX_RATING}); the larger of the two is reported
     * along with the {@link ImprovementAdvice advice} for the dimension that produced it. Two edge
     * cases override the comparison:
     * <ul>
     *   <li>A control at the maximum (5) on <em>both</em> dimensions yields an improvement of zero and is
     *       reported as {@link ImprovementAdvice#COMPLETED}.</li>
     *   <li>A control undeployed (0) on <em>both</em> dimensions is scored on raising scope and
     *       maturity together by one point, corresponding to a new deployment (reported as
     *       {@link ImprovementAdvice#DEPLOY_NEW}), since raising either alone leaves the
     *       multiplicative scaling factor at zero.</li>
     * </ul>
     * Controls with no technique coverage entries are omitted from the result.
     *
     * @param dto the assessment whose saved control statuses provide the current maturity and scope baseline
     * @return a map of each covered control to its {@link CoverageImprovement}
     */
    @Transactional
    public Map<Control, CoverageImprovement> calculateCoverageImprovements(AssessmentDTO dto) {
        Map<UUID, ControlStatusDTO> statusLookup = indexStatuses(dto, true);
        Map<UUID, Short> priorities = getTechniquePriorities(dto.getId());

        Iterable<Control> allControls = controlRepository.findAll();

        // Baseline residual failure probability per technique per coverage type, compounded from the
        // currently deployed controls. A technique/type touched by no deployed control stays at 1.0.
        Map<Technique, Map<CoverageType, Double>> residual = new HashMap<>();
        for (Control control : allControls) {
            ControlStatusDTO status = statusLookup.get(control.getId());
            if (status == null) {
                continue;
            }
            double scope = scopeOf(status);
            double maturity = maturityOf(status);
            double factor = CoverageCalculationService.effectiveScalingFactor(scope, maturity);

            for (TechniqueCoverage coverage : control.getTechniqueCoverage()) {
                double failureProb = failureProbability(factor, coverage.getCoverageRating());
                residual.computeIfAbsent(coverage.getTechnique(), t -> new EnumMap<>(CoverageType.class))
                        .merge(coverage.getCoverageType(), failureProb, (a, b) -> a * b);
            }
        }

        Map<Control, CoverageImprovement> result = new LinkedHashMap<>();

        for (Control control : allControls) {
            if (control.getTechniqueCoverage().isEmpty()) {
                continue;
            }

            ControlStatusDTO status = statusLookup.get(control.getId());
            double scope = scopeOf(status);
            double maturity = maturityOf(status);

            ImprovementAdvice advice;
            Map<Technique, Double> techniqueImprovements;

            if (scope >= MAX_RATING && maturity >= MAX_RATING) {
                // Already maxed on both dimensions: no risk reduction is achievable.
                advice = ImprovementAdvice.COMPLETED;
                techniqueImprovements = new LinkedHashMap<>();
            } else if (scope == 0.0 && maturity == 0.0) {
                // Undeployed on both dimensions: raising only one leaves the scaling factor at zero,
                // so a meaningful deployment raises scope and maturity together by one point.
                advice = ImprovementAdvice.DEPLOY_NEW;
                techniqueImprovements = riskReduction(control, scope, maturity, 1.0, 1.0, residual, priorities);
            } else if (scope >= MAX_RATING) {
                // Scope already maxed: the only achievable improvement is raising maturity.
                advice = ImprovementAdvice.MATURITY;
                techniqueImprovements = riskReduction(control, scope, maturity, scope, Math.min(maturity + 1, MAX_RATING), residual, priorities);
            } else if (maturity >= MAX_RATING) {
                // Maturity already maxed: the only achievable improvement is raising scope.
                advice = ImprovementAdvice.SCOPE;
                techniqueImprovements = riskReduction(control, scope, maturity, Math.min(scope + 1, MAX_RATING), maturity, residual, priorities);
            } else {
                Map<Technique, Double> scopeImprovements =
                        riskReduction(control, scope, maturity, Math.min(scope + 1, MAX_RATING), maturity, residual, priorities);
                Map<Technique, Double> maturityImprovements =
                        riskReduction(control, scope, maturity, scope, Math.min(maturity + 1, MAX_RATING), residual, priorities);

                // Pick whichever dimension reduces the most total risk across the control's techniques.
                if (sum(scopeImprovements) >= sum(maturityImprovements)) {
                    advice = ImprovementAdvice.SCOPE;
                    techniqueImprovements = scopeImprovements;
                } else {
                    advice = ImprovementAdvice.MATURITY;
                    techniqueImprovements = maturityImprovements;
                }
            }

            result.put(control, new CoverageImprovement(sum(techniqueImprovements), advice, techniqueImprovements));
        }

        return result;
    }

    /**
     * Computes the per-technique reduction in residual failure probability obtained by moving a single
     * control from its current scope/maturity to a target scope/maturity, then weights each reduction by
     * the technique's priority.
     * <p>
     * For every technique/{@link CoverageType} the control covers, the control's failure-probability
     * contribution is recomputed at both the current and target levels (aggregated across multiple
     * coverage entries for the same technique/type). The control's current contribution is divided out of
     * the baseline {@code residual} to isolate the rest of the deployment, the target contribution is
     * folded back in, and the difference gives the reduction. Reductions are summed across coverage types
     * per technique and finally multiplied by the technique's priority weight.
     *
     * @param control         the control being improved; its {@link TechniqueCoverage} entries define which techniques are affected
     * @param currentScope    the control's current coverage scope (0–{@value CoverageCalculationService#MAX_RATING})
     * @param currentMaturity the control's current coverage maturity (0–{@value CoverageCalculationService#MAX_RATING})
     * @param targetScope     the scope the control is being raised to
     * @param targetMaturity  the maturity the control is being raised to
     * @param residual        per-technique, per-{@link CoverageType} baseline residual failure probability compounded from all deployed controls
     * @param priorities      technique ID to priority weight (0–5); missing entries are treated as 0
     * @return a map of technique to the total priority-weighted risk reduction it gains; entries may be zero
     */
    private Map<Technique, Double> riskReduction(Control control, double currentScope, double currentMaturity,
                                                 double targetScope, double targetMaturity,
                                                 Map<Technique, Map<CoverageType, Double>> residual,
                                                 Map<UUID, Short> priorities) {
        double currentFactor = CoverageCalculationService.effectiveScalingFactor(currentScope, currentMaturity);
        double targetFactor = CoverageCalculationService.effectiveScalingFactor(targetScope, targetMaturity);

        // Aggregate this control's failure-probability factor per technique/type at both levels, in case
        // it covers the same technique/type through more than one coverage entry.
        Map<Technique, Map<CoverageType, double[]>> factors = new HashMap<>();
        for (TechniqueCoverage coverage : control.getTechniqueCoverage()) {
            double currentF = failureProbability(currentFactor, coverage.getCoverageRating());
            double targetF = failureProbability(targetFactor, coverage.getCoverageRating());

            Map<CoverageType, double[]> typeFactors = factors.get(coverage.getTechnique());
            if (typeFactors == null) {
                typeFactors = new EnumMap<>(CoverageType.class);
                factors.put(coverage.getTechnique(), typeFactors);
            }
            double[] product = typeFactors.computeIfAbsent(coverage.getCoverageType(), _ -> new double[]{1.0, 1.0});
            product[0] *= currentF;
            product[1] *= targetF;
        }

        Map<Technique, Double> gains = new LinkedHashMap<>();
        for (Map.Entry<Technique, Map<CoverageType, double[]>> techEntry : factors.entrySet()) {
            Map<CoverageType, Double> typeResidual = residual.get(techEntry.getKey());
            double techReduction = 0.0;
            for (Map.Entry<CoverageType, double[]> typeEntry : techEntry.getValue().entrySet()) {
                double currentProduct = typeEntry.getValue()[0];
                if (currentProduct <= 0.0) {
                    // Control already drives this technique/type to zero failure probability.
                    continue;
                }
                double baseline = typeResidual != null ? typeResidual.getOrDefault(typeEntry.getKey(), 1.0) : 1.0;
                double residualWithoutControl = baseline / currentProduct;
                double reduction = baseline - residualWithoutControl * typeEntry.getValue()[1];
                if (reduction > 0.0) {
                    techReduction += reduction;
                }
            }
            // Weight the reduction by the technique's priority (un-prioritised techniques weigh 0).
            short priority = priorities.getOrDefault(techEntry.getKey().getId(), (short) 0);
            gains.put(techEntry.getKey(), techReduction * priority);
        }
        return gains;
    }

    /** Sums the values of a per-technique gain map. */
    private static double sum(Map<Technique, Double> gains) {
        double total = 0.0;
        for (double value : gains.values()) {
            total += value;
        }
        return total;
    }

    /**
     * Loads an {@link Assessment} by ID or throws a {@code 404} if none exists.
     */
    private Assessment requireAssessment(UUID assessmentId) {
        Optional<Assessment> existing = assessmentRepository.findById(assessmentId);
        if (existing.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Assessment not found");
        }
        return existing.get();
    }

    /**
     * Flattens a DTO's control-status collection into a map keyed by control ID.
     *
     * @param dto       the assessment whose statuses are indexed
     * @param skipBlank when {@code true}, blank statuses (see {@link ControlStatusDTO#isBlank()}) are omitted
     * @return a map of control ID to its status; empty if the DTO has no statuses
     */
    private Map<UUID, ControlStatusDTO> indexStatuses(AssessmentDTO dto, boolean skipBlank) {
        Map<UUID, ControlStatusDTO> lookup = new HashMap<>();
        if (dto.getControlStatusMapping() != null) {
            for (ControlStatusDTO statusDTO : dto.getControlStatusMapping()) {
                if (!skipBlank || !statusDTO.isBlank()) {
                    lookup.put(statusDTO.getControlId(), statusDTO);
                }
            }
        }
        return lookup;
    }

    /** Returns the status's coverage scope, treating a missing status or null value as 0. */
    private static double scopeOf(ControlStatusDTO status) {
        if (status != null && status.getCoverageScope() != null) {
            return status.getCoverageScope();
        }
        return 0.0;
    }

    /** Returns the status's coverage maturity, treating a missing status or null value as 0. */
    private static double maturityOf(ControlStatusDTO status) {
        if (status != null && status.getCoverageMaturity() != null) {
            return status.getCoverageMaturity();
        }
        return 0.0;
    }
}
