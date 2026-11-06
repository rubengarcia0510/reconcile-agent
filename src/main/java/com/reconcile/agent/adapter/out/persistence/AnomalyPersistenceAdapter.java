package com.reconcile.agent.adapter.out.persistence;

import com.reconcile.agent.application.port.out.AnomalyRepository;
import com.reconcile.agent.domain.model.Anomaly;
import com.reconcile.agent.domain.model.AnomalyStatus;
import com.reconcile.agent.domain.model.AnomalyType;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
class AnomalyPersistenceAdapter implements AnomalyRepository {

    @Document("anomalies")
    record Doc(@Id String id, String runId, AnomalyType type, String invoiceId,
               String transactionId, String explanation, AnomalyStatus status) {}

    private final AnomalyMongoRepository repository;

    AnomalyPersistenceAdapter(AnomalyMongoRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<Anomaly> save(Anomaly a) {
        String id = a.id() != null ? a.id() : UUID.randomUUID().toString();
        return repository.save(new Doc(id, a.runId(), a.type(), a.invoiceId(),
                a.transactionId(), a.explanation(), a.status())).map(this::toDomain);
    }

    @Override
    public Mono<Anomaly> findById(String id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public Flux<Anomaly> findByRunId(String runId) {
        return repository.findByRunId(runId).map(this::toDomain);
    }

    @Override
    public Mono<Void> deleteByRunId(String runId) {
        return repository.deleteByRunId(runId);
    }

    private Anomaly toDomain(Doc d) {
        return new Anomaly(d.id(), d.runId(), d.type(), d.invoiceId(),
                d.transactionId(), d.explanation(), d.status());
    }
}
