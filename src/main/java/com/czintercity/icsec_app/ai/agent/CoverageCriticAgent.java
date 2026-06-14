package com.czintercity.icsec_app.ai.agent;

import com.anthropic.client.AnthropicClient;
import com.anthropic.models.messages.*;
import com.czintercity.icsec_app.controls.entity.Control;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

/**
 * A critic agent that reviews the raw coverage assessment produced by
 * {@link CoverageAssessmentAgent} before it is handed to the application.
 *
 * <p>Takes over responsibility of correcting its outputs, as some results were inaccurate.
 *
 * <p>Entry point: {@link #clank(Control, String)}.
 */
@Component
public class CoverageCriticAgent {

    private static final Logger log = LoggerFactory.getLogger(CoverageCriticAgent.class);
    private static final Logger llmLog = LoggerFactory.getLogger("llm");
    private static final String LOG_COMPONENT = "coverage_critic_agent";

    private static final String SYSTEM_PROMPT =
            "You are a senior cybersecurity reviewer auditing a junior analyst's coverage assessment of a security control against MITRE ATT&CK for ICS techniques.\n\n"
                    + "You shall receive the control's title and description, followed by a JSON array of coverage entries produced by the analyst. Each entry has:\n"
                    + "- `technique_id` (e.g. \"T0859\")\n"
                    + "- `technique_name`\n"
                    + "- `coverage_type` (one of: detective, preventative, deterrent, containment, recovery)\n"
                    + "- `coverage_rating` (integer 1–5)\n"
                    + "- `reasoning`\n\n"
                    + "Your job is to verify the assessment and produce a corrected version. Work through it carefully but do NOT emit your reasoning — only the final JSON array.\n\n"
                    + "For each entry, judge whether the `coverage_rating` and `reasoning` are accurate given the control and the technique's mechanics in an OT/ICS environment:\n\n"
                    + "1. OVERRIDE — If the rating is off by more than 1 point from your own assessment, OR the reasoning is factually or logically flawed, replace `coverage_rating` with your assessed value (1–5), replace `reasoning` with your own (1–3 sentences, specific to this technique and coverage type), and set `overridden: true`.\n"
                    + "2. ACCEPT — Otherwise keep the entry as-is and set `overridden: false`.\n"
                    + "3. REMOVE — Drop the entry entirely if it is nonsense: the control does not plausibly provide this coverage type for this technique, the technique is irrelevant or hallucinated, the coverage_type does not make sense for the technique, the entry is a duplicate, or your assessed rating would be 0.\n\n"
                    + "Be conservative about inflated scores: a control that only tangentially relates to a technique should not score highly. Do not invent new entries or new techniques.\n\n"
                    + "Emit a single JSON array of the surviving (accepted or overridden) entries. Each element:\n\n"
                    + "{\n"
                    + "  \"technique_id\": \"string\",\n"
                    + "  \"technique_name\": \"string\",\n"
                    + "  \"coverage_type\": \"string\",\n"
                    + "  \"coverage_rating\": integer,\n"
                    + "  \"reasoning\": \"string\",\n"
                    + "  \"overridden\": boolean\n"
                    + "}\n\n"
                    + "Return ONLY the JSON array. No prose, no markdown fences, no commentary before or after. If no entries survive, return exactly: []";

    private static final String USER_PROMPT_TEMPLATE =
            "Control Title: %s\nControl Description: %s\n\nAnalyst assessment to review:\n%s";

    private final AnthropicClient client;

    public CoverageCriticAgent(AnthropicClient client) {
        this.client = client;
    }

    /**
     * Reviews the assessment JSON produced by {@link CoverageAssessmentAgent} and
     * returns a corrected JSON array with inaccurate ratings overridden and
     * nonsensical entries removed.
     *
     * @param control        the control that was assessed, for context
     * @param assessmentJson the raw JSON array emitted by the assessment agent
     * @return the corrected JSON array string, or {@code []} if nothing survives
     */
    public String clank(Control control, String assessmentJson) {
        MDC.put("llm_component", LOG_COMPONENT);
        llmLog.info("INPUT  control='{}' (id={})\n{}", control.getName(), control.getId(), assessmentJson);
        try {
            String userMessage = String.format(
                    USER_PROMPT_TEMPLATE, control.getName(), control.getDescription(), assessmentJson);
            log.info("Critic reviewing assessment for control='{}' (id={})", control.getName(), control.getId());

            // Run on sonnet for extra power
            MessageCreateParams params = MessageCreateParams.builder()
                    .model(Model.CLAUDE_SONNET_4_6)
                    .maxTokens(8192)
                    .outputConfig(OutputConfig.builder()
                            .effort(OutputConfig.Effort.MEDIUM)
                            .build())
                    .system(SYSTEM_PROMPT)
                    .addUserMessage(userMessage)
                    .build();

            Message response = client.messages().create(params);

            String result = response.content().stream()
                    .filter(ContentBlock::isText)
                    .map(b -> b.asText().text())
                    .reduce("", String::concat);

            log.info("Critic review complete ({} chars)", result.length());
            llmLog.info("OUTPUT {}", result);
            return result;
        } finally {
            MDC.remove("llm_component");
        }
    }
}