package com.reconcile.agent.application.port.out;

import com.reconcile.agent.domain.model.Anomaly;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AnomalyRepository {
    Mono<Anomaly> save(Anomaly anomaly);

    Mono<Anomaly> findById(String id);

    Flux<Anomaly> findByRunId(String runId);

    Mono<Void> deleteByRunId(String runId);
}
