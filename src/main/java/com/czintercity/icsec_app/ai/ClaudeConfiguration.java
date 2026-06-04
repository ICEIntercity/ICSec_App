package com.czintercity.icsec_app.ai;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration class that exposes the Anthropic SDK client and shared {@link com.fasterxml.jackson.databind.ObjectMapper}
 * as application-scoped beans.
 * The API key is read from the {@code claude.api_key} application property.
 */
@Configuration
public class ClaudeConfiguration {

    @Bean
    public AnthropicClient anthropicClient(@Value("${claude.api_key}") String apiKey){
        return AnthropicOkHttpClient.builder()
                .apiKey(apiKey.strip()).
                build();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
