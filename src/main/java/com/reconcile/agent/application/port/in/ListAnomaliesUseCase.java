package com.reconcile.agent.application.port.in;

import com.reconcile.agent.domain.model.Anomaly;
import reactor.core.publisher.Flux;

public interface ListAnomaliesUseCase {
    Flux<Anomaly> list(String runId);
}
