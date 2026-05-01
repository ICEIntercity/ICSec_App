package com.czintercity.icsec_app.ai;

import com.anthropic.client.AnthropicClient;
import com.anthropic.models.messages.*;
import com.czintercity.icsec_app.ai.tools.AgentTool;
import com.czintercity.icsec_app.ai.tools.PreventativeCoverageTool;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * An agent that assesses coverage
 *
 * <p>Entry point: {@link #run(String)}.
 */
@Component
public class CoverageAssessmentAgent {

    private static final Logger log = LoggerFactory.getLogger(CoverageAssessmentAgent.class);

    /** Maximum number of agentic turns before we bail out (safety net). */
    private static final int MAX_TURNS = 10;

    private static final String SYSTEM_PROMPT =
            "You are a cybersecurity analyst specialising in ICS/OT security. "
                    + "You have access to the evaluate_preventative_coverage tool. "
                    + "When asked to evaluate controls, you MUST call that tool for each evaluation — "
                    + "do not attempt to answer from memory.";

    // -----------------------------------------------------------------------
    // Placeholder user prompt
    // -----------------------------------------------------------------------

    /**
     * Placeholder prompt that provides three complete, valid tool inputs and
     * forces the agent to invoke the tool for at least the first one.
     *
     * <p>Replace or parameterise this at call-sites as needed.
     */
    public static final String PLACEHOLDER_PROMPT =
            "I need you to evaluate the following control–technique pairs using the "
                    + "evaluate_preventative_coverage tool. "
                    + "Start by calling the tool immediately for the FIRST pair, then handle the "
                    + "remaining two pairs.\n\n"

                    + "--- Pair 1 ---\n"
                    + "Technique ID   : T0859\n"
                    + "Technique Name : Valid Accounts\n"
                    + "Technique Desc : Adversaries may obtain and abuse credentials of existing accounts as "
                    +                   "a means of gaining initial access, persistence, privilege escalation, "
                    +                   "or defense evasion. Compromised credentials may be used to bypass "
                    +                   "access controls placed on various resources on systems within the "
                    +                   "network and may even be used for persistent access to remote systems.\n"
                    + "Control Name   : Multi-Factor Authentication (MFA)\n"
                    + "Control Desc   : Require all remote and privileged local access to OT systems to use "
                    +                   "a second authentication factor (e.g. TOTP, hardware token) in "
                    +                   "addition to a password.\n\n"

                    + "--- Pair 2 ---\n"
                    + "Technique ID   : T0814\n"
                    + "Technique Name : Denial of Service\n"
                    + "Technique Desc : Adversaries may perform Denial-of-Service (DoS) attacks to degrade "
                    +                   "or block the availability of targeted resources to users. DoS can "
                    +                   "target OT networks or devices to disrupt operations in the "
                    +                   "physical environment.\n"
                    + "Control Name   : Network Segmentation\n"
                    + "Control Desc   : Enforce strict Layer-2 and Layer-3 segmentation between IT and OT "
                    +                   "networks using unidirectional gateways or industrial firewalls with "
                    +                   "default-deny rule sets. Limit cross-zone traffic to the minimum "
                    +                   "required for operations.\n\n"

                    + "--- Pair 3 ---\n"
                    + "Technique ID   : T0862\n"
                    + "Technique Name : Supply Chain Compromise\n"
                    + "Technique Desc : Adversaries may perform supply chain compromise to gain access to "
                    +                   "target systems prior to delivery. Targeting may involve "
                    +                   "manipulation of hardware, software, or firmware at any step in the "
                    +                   "supply chain from development through normal distribution "
                    +                   "channels to the end consumer.\n"
                    + "Control Name   : Software Bill of Materials (SBOM) and Integrity Verification\n"
                    + "Control Desc   : Maintain a complete SBOM for all OT software and firmware. Verify "
                    +                   "cryptographic signatures of updates before deployment and audit "
                    +                   "third-party components against known-vulnerability databases on a "
                    +                   "regular schedule.\n\n"

                    + "After all three evaluations, summarise the results in a table.";

    // -----------------------------------------------------------------------
    // Fields
    // -----------------------------------------------------------------------

    private final AnthropicClient client;
    private final PreventativeCoverageTool coverageTool;
    private final ObjectMapper mapper;

    private final List<ToolUnion> registeredTools;

    // -----------------------------------------------------------------------
    // Constructor
    // -----------------------------------------------------------------------

    public CoverageAssessmentAgent(AnthropicClient client,
                                   PreventativeCoverageTool coverageTool,
                                   ObjectMapper mapper) {
        this.client       = client;
        this.coverageTool = coverageTool;
        this.mapper       = mapper;
        this.registeredTools = buildToolList();
    }

    // -----------------------------------------------------------------------
    // Public API
    // -----------------------------------------------------------------------

    /**
     * Runs the agent with {@link #PLACEHOLDER_PROMPT} and prints the final
     * response to stdout.  Useful for quick smoke-testing.
     */
    public String runWithPlaceholder() {
        return run(PLACEHOLDER_PROMPT);
    }

    /**
     * Drives the agentic loop for the supplied {@code userMessage}.
     *
     * <ol>
     *   <li>Sends the message to Claude.</li>
     *   <li>If Claude replies with one or more {@code tool_use} blocks, each
     *       tool is dispatched to the matching {@link AgentTool} and the
     *       results are fed back as {@code tool_result} messages.</li>
     *   <li>The loop continues until the stop reason is {@code end_turn} or
     *       {@link #MAX_TURNS} is reached.</li>
     * </ol>
     *
     * @param userMessage the initial user message
     * @return the final text response from the model
     */
    public String run(String userMessage) {
        log.info("Agent starting. User message length={}", userMessage.length());

        // Build a mutable conversation history.
        List<MessageParam> history = new ArrayList<>();
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

            // Append the assistant turn to history.
            history.add(assistantMessageParam(response));

            // Collect any tool_use blocks in this response.
            List<ToolUseBlock> toolUseBlocks = response.content().stream()
                    .filter(ContentBlock::isToolUse)
                    .map(ContentBlock::asToolUse)
                    .toList();

            if (toolUseBlocks.isEmpty()) {
                // No more tool calls – gather all text blocks and return.
                finalResponse = response.content().stream()
                        .filter(ContentBlock::isText)
                        .map(b -> b.asText().text())
                        .reduce("", (a, b) -> a + b);
                log.info("Agent finished after {} turn(s).", turn);
                break;
            }

            // Dispatch each tool call and collect results.
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

            // Feed all results back as a single user turn.
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

    /** Sends the current conversation history to Claude and returns the reply. */
    private Message callClaude(List<MessageParam> history) {
        MessageCreateParams params = MessageCreateParams.builder()
                .model(Model.CLAUDE_SONNET_4_6)
                .maxTokens(4096)
                .system(SYSTEM_PROMPT)
                .messages(history)
                .tools(registeredTools)
                .build();

        return client.messages().create(params);
    }

    /**
     * Converts a {@link Message} to a {@link MessageParam} so it can be
     * appended to the conversation history.
     */
    private static MessageParam assistantMessageParam(Message message) {
        // Serialize each content block back to the param shape Claude expects.
        List<ContentBlockParam> params = message.content().stream()
                .map(CoverageAssessmentAgent::toContentBlockParam)
                .toList();

        return MessageParam.builder()
                .role(MessageParam.Role.ASSISTANT)
                .content(MessageParam.Content.ofBlockParams(params))
                .build();
    }

    /** Converts a raw {@link ContentBlock} to its {@link ContentBlockParam} equivalent. */
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

    /** Wraps a list of tool results into a single user-role {@link MessageParam}. */
    private static MessageParam toolResultMessageParam(List<ToolResultBlockParam> results) {
        List<ContentBlockParam> content = results.stream()
                .map(r -> ContentBlockParam.ofToolResult(r))
                .toList();

        return MessageParam.builder()
                .role(MessageParam.Role.USER)
                .content(MessageParam.Content.ofBlockParams(content))
                .build();
    }

    /**
     * Routes a {@link ToolUseBlock} to the correct {@link AgentTool} and
     * executes it.  Currently only {@link PreventativeCoverageTool} is
     * registered; extend this method when additional tools are added.
     */
    private AgentTool.ToolResult dispatchTool(ToolUseBlock toolUse) {
        String name = toolUse.name();

        if (coverageTool.getName().equals(name)) {
            Map<String, Object> inputMap = toStringObjectMap(toolUse._input());
            return coverageTool.clank(inputMap);
        }

        log.warn("Unknown tool requested: {}", name);
        return AgentTool.ToolResult.error("Unknown tool: " + name);
    }

    /**
     * Converts the raw tool input (which the SDK exposes as an opaque object)
     * to a {@code Map<String, Object>} so it can be passed to
     * {@link AgentTool#clank(Map)}.
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> toStringObjectMap(Object rawInput) {
        return mapper.convertValue(rawInput, new TypeReference<Map<String, Object>>() {});
    }

    /** Builds the tool list **/
    private List<ToolUnion> buildToolList() {
        Tool tool = Tool.builder()
                .name(coverageTool.getName())
                .description(coverageTool.getDescription())
                .inputSchema(coverageTool.getInputSchema())
                .build();
        return List.of(ToolUnion.ofTool(tool));
    }
}