package com.czintercity.icsec_app.assessment.service;

import com.czintercity.icsec_app.assessment.dto.MitreCoverageDTO;
import com.czintercity.icsec_app.assessment.dto.util.TacticCoverageScore;
import com.czintercity.icsec_app.assessment.dto.util.TechniqueCoverageScore;
import com.czintercity.icsec_app.assessment.entity.Assessment;
import com.czintercity.icsec_app.assessment.entity.ControlStatus;
import com.czintercity.icsec_app.assessment.exception.BlankAssessmentException;
import com.czintercity.icsec_app.attack.entity.Tactic;
import com.czintercity.icsec_app.attack.entity.Technique;
import com.czintercity.icsec_app.attack.repository.TacticRepository;
import com.czintercity.icsec_app.controls.entity.Control;
import com.czintercity.icsec_app.relationships.techniqueCoverage.CoverageType;
import com.czintercity.icsec_app.relationships.techniqueCoverage.entity.TechniqueCoverage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
public class CoverageCalculationService {

    private final TacticRepository tacticRepository;

    public CoverageCalculationService(TacticRepository tacticRepository) {
        this.tacticRepository = tacticRepository;
    }

    @Transactional
    public MitreCoverageDTO calculateMitreCoverage(Assessment assessment) {
        if (assessment == null || assessment.getControlStatusMapping() == null) {
            throw new BlankAssessmentException("Missing assessment or control status mapping.");
        }

        // Initialize a TacticCoverageScore per tactic, with a blank TechniqueCoverageScore per technique
        Map<Tactic, TacticCoverageScore> coverageScores = new HashMap<>();
        for (Tactic tactic : tacticRepository.findAll()) {
            TacticCoverageScore tacticScore = new TacticCoverageScore();
            for (Technique technique : tactic.getTechniques()) {
                tacticScore.techniqueCoverageScores.put(technique, new TechniqueCoverageScore());
            }
            coverageScores.put(tactic, tacticScore);
        }

        // Multiply each technique's failure probability by the control's effective reduction
        for (ControlStatus controlStatus : assessment.getControlStatusMapping()) {
            Control control = controlStatus.getControl();
            Short scope = controlStatus.getCoverageScope();
            Short maturity = controlStatus.getCoverageMaturity();

            for (TechniqueCoverage coverage : control.getTechniqueCoverage()) {
                Technique technique = coverage.getTechnique();
                Short coverageRating = coverage.getCoverageRating();
                CoverageType coverageType = coverage.getCoverageType();

                // Effective coverage formula - exponents weight scope more heavily than maturity
                double effectiveCoverageScore = (Math.pow(scope, 0.65) * Math.pow(maturity, 0.35) / 5) * coverageRating;
                double effectiveFailureProbability = Math.max(0.0, 1 - (effectiveCoverageScore / 5));

                for (Tactic tactic : technique.getTactics()) {
                    TechniqueCoverageScore techniqueScore = coverageScores.get(tactic).techniqueCoverageScores.get(technique);
                    double current = techniqueScore.typeFailureProbabilities.get(coverageType);
                    techniqueScore.typeFailureProbabilities.put(coverageType, current * effectiveFailureProbability);
                }
            }
        }

        MitreCoverageDTO dto = new MitreCoverageDTO();
        dto.setAssessment(assessment);
        dto.setCoverageScores(coverageScores);
        return dto;
    }
}