package com.reconcile.agent.adapter.in.web;

import com.reconcile.agent.application.port.in.AnalyzeRunUseCase;
import com.reconcile.agent.application.port.in.ListAnomaliesUseCase;
import com.reconcile.agent.application.port.in.ReviewAnomalyUseCase;
import com.reconcile.agent.domain.model.AnalysisResult;
import com.reconcile.agent.domain.model.Anomaly;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api")
public class AnomalyController {

    private final AnalyzeRunUseCase analyzeUseCase;
    private final ListAnomaliesUseCase listUseCase;
    private final ReviewAnomalyUseCase reviewUseCase;

    public AnomalyController(AnalyzeRunUseCase analyzeUseCase, ListAnomaliesUseCase listUseCase,
                             ReviewAnomalyUseCase reviewUseCase) {
        this.analyzeUseCase = analyzeUseCase;
        this.listUseCase = listUseCase;
        this.reviewUseCase = reviewUseCase;
    }

    @PostMapping("/runs/{runId}/analyze")
    public Mono<AnalysisResult> analyze(@PathVariable("runId") String runId) {
        return analyzeUseCase.analyze(runId);
    }

    @GetMapping("/runs/{runId}/anomalies")
    public Flux<Anomaly> list(@PathVariable("runId") String runId) {
        return listUseCase.list(runId);
    }

    @PostMapping("/anomalies/{id}/approve")
    public Mono<Anomaly> approve(@PathVariable("id") String id) {
        return reviewUseCase.approve(id);
    }

    @PostMapping("/anomalies/{id}/reject")
    public Mono<Anomaly> reject(@PathVariable("id") String id) {
        return reviewUseCase.reject(id);
    }
}
