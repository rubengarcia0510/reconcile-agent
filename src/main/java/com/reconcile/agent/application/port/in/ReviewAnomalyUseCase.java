package com.reconcile.agent.application.port.in;

import com.reconcile.agent.domain.model.Anomaly;
import reactor.core.publisher.Mono;

public interface ReviewAnomalyUseCase {
    Mono<Anomaly> approve(String anomalyId);

    Mono<Anomaly> reject(String anomalyId);
}
