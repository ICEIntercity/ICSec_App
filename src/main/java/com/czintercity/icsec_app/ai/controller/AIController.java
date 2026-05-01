package com.czintercity.icsec_app.ai.controller;

import com.anthropic.client.AnthropicClient;
import com.czintercity.icsec_app.ai.CoverageAssessmentAgent;
import com.czintercity.icsec_app.ai.tools.AgentTool;
import com.czintercity.icsec_app.ai.tools.PreventativeCoverageTool;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.HashMap;
import java.util.Map;

@Controller
public class AIController {

    public final AnthropicClient client;
    public final PreventativeCoverageTool preventativeCoverageTool;
    public final ObjectMapper objectMapper;
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public AIController(AnthropicClient client, PreventativeCoverageTool preventativeCoverageTool, ObjectMapper objectMapper) {
        this.client = client;
        this.preventativeCoverageTool = preventativeCoverageTool;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/ai/test")
    public String testAI(Model model) {
        CoverageAssessmentAgent agent = new CoverageAssessmentAgent(client, preventativeCoverageTool, objectMapper);
        String out = "AI failed.";

        try {
            log.info("Clanking, please wait...");
            out = agent.runWithPlaceholder();
            log.info(out);
        } catch (Exception e) {
            log.info(e.getMessage());
        }


        /*
        Map<String, Object> input = new HashMap<>();
        input.put("technique_id", "T0859");
        input.put("technique_name", "Valid Accounts");
        input.put("technique_description", "Adversaries may obtain and abuse credentials of existing accounts as a means of gaining initial access, persistence, privilege escalation, or defense evasion.");
        input.put("control_name", "Multi-Factor Authentication (MFA)");
        input.put("control_description", "Require all remote and privileged local access to OT systems to use a second authentication factor (e.g. TOTP, hardware token) in addition to a password.");
        try{
            AgentTool.ToolResult result = preventativeCoverageTool.clank(input);
            return result.toString();
        }
        catch (Exception e){
            log.error(e.getMessage());
        }
        */
        return out;
    }
}
