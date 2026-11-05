package com.reconcile.agent.adapter.out.llm;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@EnableConfigurationProperties(VultrInferenceProperties.class)
class VultrInferenceConfig {

    @Bean
    WebClient vultrInferenceWebClient(VultrInferenceProperties props) {
        if (props.apiKey() == null || props.apiKey().isBlank()) {
            throw new IllegalStateException("VULTR_INFERENCE_API_KEY is not set");
        }
        return WebClient.builder()
                .baseUrl(props.baseUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + props.apiKey().trim())
                .build();
    }
}
