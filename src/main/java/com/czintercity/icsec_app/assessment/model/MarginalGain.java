package com.czintercity.icsec_app.assessment.model;

import com.czintercity.icsec_app.attack.entity.Technique;

import java.util.Map;

/**
 * The marginal coverage gain achievable for a single control, taken as the greater of the two
 * available improvement paths: raising the control's deployment scope by one step or raising its
 * implementation maturity by one step (neither beyond the maximum of 5).
 *
 * @param totalGain       the summed gain across all covered techniques for the chosen dimension
 * @param advice          which dimension ({@link ImprovementAdvice#SCOPE} or {@link ImprovementAdvice#MATURITY})
 *                        produced the greater gain and is therefore recommended
 * @param techniqueGains  the per-technique breakdown of the gain for the chosen dimension
 */
public record MarginalGain(double totalGain, ImprovementAdvice advice, Map<Technique, Double> techniqueGains) {
}