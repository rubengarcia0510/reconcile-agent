package com.reconcile.agent.application.service;

import com.reconcile.agent.application.port.in.ListAnomaliesUseCase;
import com.reconcile.agent.application.port.out.AnomalyRepository;
import com.reconcile.agent.domain.model.Anomaly;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class ListAnomaliesService implements ListAnomaliesUseCase {

    private final AnomalyRepository anomalies;

    public ListAnomaliesService(AnomalyRepository anomalies) {
        this.anomalies = anomalies;
    }

    @Override
    public Flux<Anomaly> list(String runId) {
        return anomalies.findByRunId(runId);
    }
}
