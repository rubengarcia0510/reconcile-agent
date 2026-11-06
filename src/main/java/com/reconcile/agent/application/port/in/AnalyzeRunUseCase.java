package com.reconcile.agent.application.port.in;

import com.reconcile.agent.domain.model.AnalysisResult;
import reactor.core.publisher.Mono;

public interface AnalyzeRunUseCase {
    Mono<AnalysisResult> analyze(String runId);
}
