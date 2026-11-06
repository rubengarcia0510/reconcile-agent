package com.reconcile.agent.adapter.out.persistence;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AnomalyMongoRepository extends ReactiveMongoRepository<AnomalyPersistenceAdapter.Doc, String> {
    Flux<AnomalyPersistenceAdapter.Doc> findByRunId(String runId);

    Mono<Void> deleteByRunId(String runId);
}
