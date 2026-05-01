package com.czintercity.icsec_app.ai;

import com.anthropic.client.AnthropicClient;
import com.anthropic.models.messages.*;
import com.czintercity.icsec_app.ai.tools.AgentTool;
import com.czintercity.icsec_app.controls.entity.Control;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * An agent that assesses the MITRE ATT&CK for ICS coverage of a security control.
 *
 * <p>Entry point: {@link #clank(Control)}.
 */
@Component
public class CoverageAssessmentAgent {

    private static final Logger log = LoggerFactory.getLogger(CoverageAssessmentAgent.class);

    /** Maximum number of agentic turns before termination. */
    private static final int MAX_TURNS = 10;

    private static final String SYSTEM_PROMPT =
            "You are a cybersecurity analyst agent specialising in evaluating the security coverage of controls in a threat-driven fashion, using MITRE ATT&CK for ICS as the reference framework.\n\n"
                    + "The user will provide a control title and description. Your task is to produce a structured evaluation of how well the control performs against relevant MITRE ATT&CK for ICS techniques, using the tools provided.\n\n"
                    + "Follow the steps below exactly and in order. Do not skip steps or reorder them.\n\n"
                    + "---\n\n"
                    + "## Step 1 — Technique Discovery\n\n"
                    + "Call `map_control_to_ics_techniques` with `control_title` and `control_description` as provided by the user.\n\n"
                    + "The tool returns an object with a `candidate_techniques` array. Each element has:\n"
                    + "- `technique_id`, `technique_title`, `technique_description`\n"
                    + "- `mapped_categories` — a list of one or more of: \"Detective\", \"Preventative\", \"Deterrent\", \"Containment\", \"Recovery\"\n\n"
                    + "If `candidate_techniques` is empty, emit `[]` and stop. "
                    + "Process only the techniques returned by this tool. Do not add, infer, or substitute techniques from your own knowledge.\n\n"
                    + "---\n\n"
                    + "## Step 2 — Coverage Evaluation\n\n"
                    + "For each (technique, category) pair from Step 1, call the corresponding tool by lowercasing the category value and wrapping it as `evaluate_<category>_coverage` (e.g. \"Detective\" → `evaluate_detective_coverage`). Pass `technique_title` as `technique_name`. You may call tools in parallel. Do NOT call a tool for any category not listed for that technique.\n\n"
                    + "If a tool call fails, treat it as `coverage_rating: 0` (discarded in Step 3).\n\n"
                    + "---\n\n"
                    + "## Step 3 — Coverage Verification\n\n"
                    + "Each coverage tool returns `coverage_rating` (0–5) and `coverage_justification`. For each response, assess whether the rating and justification are accurate given the control and the technique's mechanics in an OT/ICS environment.\n\n"
                    + "If your assessed score differs by more than 1 point from `coverage_rating`, OR the justification is factually or logically flawed:\n"
                    + "  → Override `coverage_rating` with your assessed value, replace `coverage_justification` with your own (1–3 sentences, specific to this technique and category), and set `overridden: true`.\n\n"
                    + "Discard any entry whose final `coverage_rating` is 0.\n\n"
                    + "---\n\n"
                    + "## Step 4 — Output\n\n"
                    + "Emit a single JSON array. Each element:\n\n"
                    + "{\n"
                    + "  \"technique_id\": \"string\",    // e.g. \"T0859\"\n"
                    + "  \"technique_name\": \"string\",  // technique_title from Step 1\n"
                    + "  \"coverage_type\": \"string\",   // lowercase mapped_categories value\n"
                    + "  \"coverage_score\": integer,     // final coverage_rating (1–5)\n"
                    + "  \"reasoning\": \"string\",        // final coverage_justification\n"
                    + "  \"overridden\": boolean\n"
                    + "}\n\n"
                    + "Return ONLY the JSON array. No prose, no markdown fences, no commentary before or after.";

    private static final String USER_PROMPT_TEMPLATE =
            "Control Title: %s\nControl Description: %s";

    // -----------------------------------------------------------------------
    // Fields
    // -----------------------------------------------------------------------

    private final AnthropicClient client;
    private final List<AgentTool> tools;
    private final ObjectMapper mapper;
    private final List<ToolUnion> registeredTools;

    // -----------------------------------------------------------------------
    // Constructor
    // -----------------------------------------------------------------------

    public CoverageAssessmentAgent(AnthropicClient client,
                                   List<AgentTool> tools,
                                   ObjectMapper mapper) {
        this.client = client;
        this.tools = tools;
        this.mapper = mapper;
        this.registeredTools = buildToolList();
    }

    // -----------------------------------------------------------------------
    // Public API
    // -----------------------------------------------------------------------

    /**
     * Drives the agentic coverage-assessment loop for the supplied {@link Control}.
     *
     * <ol>
     *   <li>Builds a user message from the control's name and description.</li>
     *   <li>Sends the message to Claude.</li>
     *   <li>If Claude replies with one or more {@code tool_use} blocks, each
     *       tool is dispatched to the matching {@link AgentTool} and the
     *       results are fed back as {@code tool_result} messages.</li>
     *   <li>The loop continues until the stop reason is {@code end_turn} or
     *       {@link #MAX_TURNS} is reached.</li>
     * </ol>
     *
     * @param control the security control to evaluate
     * @return the final JSON array response from the model
     */
    public String clank(Control control) {
        String userMessage = String.format(USER_PROMPT_TEMPLATE, control.getName(), control.getDescription());
        log.info("Agent starting. Control='{}' (id={})", control.getName(), control.getId());

        List<MessageParam> history = new ArrayList<>();

        // Add the parameters for the call
        history.add(MessageParam.builder()
                .role(MessageParam.Role.USER)
                .content(userMessage)
                .build());

        String finalResponse = "";
        int turn = 0;

        while (turn < MAX_TURNS) {
            turn++;
            log.debug("Turn {}", turn);

            Message response = callClaude(history);
            log.debug("Stop reason: {}", response.stopReason());

            history.add(assistantMessageParam(response));

            List<ToolUseBlock> toolUseBlocks = response.content().stream()
                    .filter(ContentBlock::isToolUse)
                    .map(ContentBlock::asToolUse)
                    .toList();

            if (toolUseBlocks.isEmpty()) {
                finalResponse = response.content().stream()
                        .filter(ContentBlock::isText)
                        .map(b -> b.asText().text())
                        .reduce("", (a, b) -> a + b);
                log.info("Agent finished after {} turn(s).", turn);
                break;
            }

            List<ToolResultBlockParam> toolResults = new ArrayList<>();
            for (ToolUseBlock toolUse : toolUseBlocks) {
                log.info("Tool called: {} (id={})", toolUse.name(), toolUse.id());
                AgentTool.ToolResult result = dispatchTool(toolUse);
                log.debug("Tool result success={} content={}", result.isSuccess(), result.getContent());

                toolResults.add(ToolResultBlockParam.builder()
                        .toolUseId(toolUse.id())
                        .content(result.getContent())
                        .isError(!result.isSuccess())
                        .build());
            }

            history.add(toolResultMessageParam(toolResults));
        }

        if (turn >= MAX_TURNS && finalResponse.isEmpty()) {
            log.warn("Reached MAX_TURNS ({}) without an end_turn stop reason.", MAX_TURNS);
            finalResponse = "[Agent reached maximum turn limit without completing]";
        }

        return finalResponse;
    }

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    private Message callClaude(List<MessageParam> history) {
        MessageCreateParams params = MessageCreateParams.builder()
                .model(Model.CLAUDE_HAIKU_4_5)
                .maxTokens(8192)
                .system(SYSTEM_PROMPT)
                .messages(history)
                .tools(registeredTools)
                .build();

        return client.messages().create(params);
    }

    private static MessageParam assistantMessageParam(Message message) {
        List<ContentBlockParam> params = message.content().stream()
                .map(CoverageAssessmentAgent::toContentBlockParam)
                .toList();

        return MessageParam.builder()
                .role(MessageParam.Role.ASSISTANT)
                .content(MessageParam.Content.ofBlockParams(params))
                .build();
    }

    private static ContentBlockParam toContentBlockParam(ContentBlock block) {
        if (block.isText()) {
            return ContentBlockParam.ofText(
                    TextBlockParam.builder()
                            .text(block.asText().text())
                            .build());
        }
        if (block.isToolUse()) {
            ToolUseBlock tu = block.asToolUse();
            return ContentBlockParam.ofToolUse(
                    ToolUseBlockParam.builder()
                            .id(tu.id())
                            .name(tu.name())
                            .input(tu._input())
                            .build());
        }
        throw new IllegalArgumentException("Unsupported content block type: " + block.getClass());
    }

    private static MessageParam toolResultMessageParam(List<ToolResultBlockParam> results) {
        List<ContentBlockParam> content = results.stream()
                .map(ContentBlockParam::ofToolResult)
                .toList();

        return MessageParam.builder()
                .role(MessageParam.Role.USER)
                .content(MessageParam.Content.ofBlockParams(content))
                .build();
    }

    private AgentTool.ToolResult dispatchTool(ToolUseBlock toolUse) {
        String name = toolUse.name();
        log.info("Dispatching tool: {} (id={})", name, toolUse.id());

        return tools.stream()
                .filter(t -> t.getName().equals(name))
                .findFirst()
                .map(t -> t.clank(toStringObjectMap(toolUse._input())))
                .orElseGet(() -> {
                    log.warn("Unknown tool requested: {}", name);
                    return AgentTool.ToolResult.error("Unknown tool: " + name);
                });
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> toStringObjectMap(Object rawInput) {
        return mapper.convertValue(rawInput, new TypeReference<Map<String, Object>>() {});
    }

    private List<ToolUnion> buildToolList() {
        List<ToolUnion> toolUnions = new ArrayList<>();
        for (AgentTool tool : tools) {
            Tool toolDefinition = Tool.builder()
                    .name(tool.getName())
                    .description(tool.getDescription())
                    .inputSchema(tool.getInputSchema())
                    .build();
            toolUnions.add(ToolUnion.ofTool(toolDefinition));
        }
        return toolUnions;
    }
}