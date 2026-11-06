package com.reconcile.agent.application.service;

import com.reconcile.agent.application.ConflictException;
import com.reconcile.agent.application.NotFoundException;
import com.reconcile.agent.application.port.in.ReviewAnomalyUseCase;
import com.reconcile.agent.application.port.out.AnomalyRepository;
import com.reconcile.agent.domain.model.Anomaly;
import com.reconcile.agent.domain.model.AnomalyStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class ReviewAnomalyService implements ReviewAnomalyUseCase {

    private final AnomalyRepository anomalies;

    public ReviewAnomalyService(AnomalyRepository anomalies) {
        this.anomalies = anomalies;
    }

    @Override
    public Mono<Anomaly> approve(String anomalyId) {
        return review(anomalyId, AnomalyStatus.APPROVED);
    }

    @Override
    public Mono<Anomaly> reject(String anomalyId) {
        return review(anomalyId, AnomalyStatus.REJECTED);
    }

    private Mono<Anomaly> review(String anomalyId, AnomalyStatus status) {
        return anomalies.findById(anomalyId)
                .switchIfEmpty(Mono.error(new NotFoundException("Anomaly not found: " + anomalyId)))
                .flatMap(anomaly -> anomaly.status() != AnomalyStatus.PENDING
                        ? Mono.error(new ConflictException("Anomaly was already " + anomaly.status()))
                        : anomalies.save(anomaly.withStatus(status)));
    }
}
