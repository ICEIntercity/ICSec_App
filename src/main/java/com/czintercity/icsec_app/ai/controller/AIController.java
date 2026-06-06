package com.czintercity.icsec_app.ai.controller;

import com.czintercity.icsec_app.ai.CoverageAssessmentAgent;
import com.czintercity.icsec_app.ai.service.AIService;
import com.czintercity.icsec_app.ai.utils.AIUtils;
import com.czintercity.icsec_app.controls.entity.Control;
import com.czintercity.icsec_app.relationships.techniqueCoverage.entity.TechniqueCoverage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

/**
 * Controller exposing AI-powered coverage assessment endpoints.
 * Delegates to {@link com.czintercity.icsec_app.ai.CoverageAssessmentAgent} for inference and falls back
 * to a secondary extractor agent when direct JSON parsing fails.
 */
@Controller
public class AIController {

    private final CoverageAssessmentAgent coverageAssessmentAgent;
    private final AIService aiService;
    private final boolean aiAvailable;
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public AIController(CoverageAssessmentAgent coverageAssessmentAgent,
                        AIService aiService,
                        @Value("${claude.api_key:}") String claudeApiKey) {
        this.coverageAssessmentAgent = coverageAssessmentAgent;
        this.aiService = aiService;
        this.aiAvailable = !claudeApiKey.isBlank();
    }

    private void requireAi() {
        if (!aiAvailable) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "AI service is not configured");
        }
    }

    /**
     * Asynchronous endpoint to assess a control's coverage via Ajax.
     * Takes a Control object, processes it through the AI agent, and returns
     * a template fragment containing the resulting coverage list.
     */
    @PostMapping("/ai/assess-coverage")
    public String assessControlCoverage(@RequestParam String name, @RequestParam String description, Model model) {
        requireAi();

        // Create a transient control object to pass to existing logic
        Control control = new Control();
        control.setName(name);
        control.setDescription(description);

        log.info("Starting AI assessment from ephemeral control object `{}`", name);

        String agentJson = coverageAssessmentAgent.clank(control);
        List<TechniqueCoverage> coverages = extractAndParse(agentJson, control);

        model.addAttribute("coverageList", coverages);
        model.addAttribute("controlName", name);

        return "fragments/ai :: coverageTable";
    }

    /**
     * Test endpoint that loads pre-computed assessment output from
     * {@code resources/test/assessment_out.json} and renders the coverage
     * fragment without making any AI calls.
     */
    @PostMapping("/ai/test-render")
    public String testRender(@RequestParam String name, @RequestParam String description, Model model) throws IOException {

        // Create a transient control object for the parser
        Control control = new Control();
        control.setName(name);
        control.setDescription(description);

        ClassPathResource resource = new ClassPathResource("test/assessment_out.json");
        String json = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        log.info("Loaded test assessment JSON ({} chars)", json.length());

        List<TechniqueCoverage> coverages = extractAndParse(json, control);

        model.addAttribute("coverageList", coverages);
        model.addAttribute("controlName", name);

        return "fragments/ai :: coverageTable";
    }

    private List<TechniqueCoverage> extractAndParse(String rawJson, Control control) {
        Optional<String> regexExtracted = AIUtils.extractJson(rawJson);
        if (regexExtracted.isPresent()) {
            try {
                return aiService.parseAssessmentOutput(regexExtracted.get(), control);
            } catch (Exception e) {
                log.warn("Regex-extracted JSON failed to parse, falling back to AI extractor: {}", e.getMessage());
                return aiService.invokeExtractorAgent(rawJson, control);
            }
        } else {
            log.warn("No JSON found by regex, falling back to AI extractor");
            return aiService.invokeExtractorAgent(rawJson, control);
        }
    }
}
