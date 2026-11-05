package com.reconcile.agent.adapter.out.llm;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "vultr.inference")
public record VultrInferenceProperties(String baseUrl, String apiKey, String model, Integer maxTokens) {}
